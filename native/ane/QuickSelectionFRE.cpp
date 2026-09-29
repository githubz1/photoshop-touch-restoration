#include "QuickSelectionFRE.hpp"

#include <algorithm>
#include <cstddef>
#include <cstdint>
#include <cstring>
#include <strings.h>
#include <sys/sysctl.h>
#include <limits>
#include <memory>
#include <vector>

#include "../quickselection/QuickSelectTool.hpp"
#include "../quickselection/GraphCut.hpp"
#include "GLContextBridge.hpp"

namespace {

using pstouch::quickselection::QuickSelectTool;
using pstouch::quickselection::adobeBKSelfTestPassed;
using pstouch::quickselection::adobeBKSelfTestFailure;

struct QuickSelectionContext {
    std::unique_ptr<QuickSelectTool> tool;
};

QuickSelectionContext* state(FREContext ctx)
{
    void* data = nullptr;

    if (ctx == nullptr ||
        FREGetContextNativeData(ctx, &data) != FRE_OK) {
        return nullptr;
    }

    return static_cast<QuickSelectionContext*>(data);
}

FREObject makeBool(bool value)
{
    FREObject result = nullptr;

    if (FRENewObjectFromBool(value ? 1u : 0u, &result) != FRE_OK) {
        return nullptr;
    }

    return result;
}

bool getInt32(FREObject object, int& value)
{
    int32_t v = 0;

    if (FREGetObjectAsInt32(object, &v) != FRE_OK) {
        return false;
    }

    value = static_cast<int>(v);
    return true;
}

bool getUint32(FREObject object, uint32_t& value)
{
    return FREGetObjectAsUint32(object, &value) == FRE_OK;
}

bool getBool(FREObject object, bool& value)
{
    uint32_t v = 0;

    if (FREGetObjectAsBool(object, &v) != FRE_OK) {
        return false;
    }

    value = v != 0;
    return true;
}

bool checkedPixelCount(
    int width,
    int height,
    std::size_t& count
)
{
    if (width <= 0 || height <= 0) {
        return false;
    }

    const std::size_t w = static_cast<std::size_t>(width);
    const std::size_t h = static_cast<std::size_t>(height);

    if (w > std::numeric_limits<std::size_t>::max() / h) {
        return false;
    }

    count = w * h;
    return true;
}

FREObject makeByteArray(
    const uint8_t* bytes,
    std::size_t size
)
{
    if (bytes == nullptr ||
        size > std::numeric_limits<uint32_t>::max()) {
        return nullptr;
    }

    FREByteArray data{};
    data.length = static_cast<uint32_t>(size);
    data.bytes = const_cast<uint8_t*>(bytes);

    FREObject result = nullptr;

    if (FRENewByteArray(&data, &result) != FRE_OK) {
        return nullptr;
    }

    return result;
}

FREObject makeMaskResult(
    const QuickSelectTool& tool,
    const uint8_t* mask,
    uint32_t format
)
{
    if (mask == nullptr) {
        return nullptr;
    }

    std::size_t count = 0;

    if (!checkedPixelCount(tool.width(), tool.height(), count)) {
        return nullptr;
    }

    // Recovered QuickSelectionCreateMaskResult behavior:
    //
    // format == 1:
    //     zero     -> 0x00000000
    //     non-zero -> 0xFFFFFFFF
    //
    // otherwise:
    //     return the raw one-byte mask.
    if (format != 1) {
        return makeByteArray(mask, count);
    }

    if (count >
        std::numeric_limits<std::size_t>::max() / 4u) {
        return nullptr;
    }

    std::vector<uint8_t> argb(count * 4u);

    for (std::size_t i = 0; i < count; ++i) {
        const uint8_t value = mask[i] != 0 ? 0xFFu : 0x00u;

        argb[i * 4u + 0u] = value;
        argb[i * 4u + 1u] = value;
        argb[i * 4u + 2u] = value;
        argb[i * 4u + 3u] = value;
    }

    return makeByteArray(argb.data(), argb.size());
}

bool decodePoints(
    FREObject array,
    std::vector<int>& x,
    std::vector<int>& y
)
{
    uint32_t length = 0;

    if (FREGetArrayLength(array, &length) != FRE_OK) {
        return false;
    }

    // Original wrapper computes count = length >> 1.
    const uint32_t count = length >> 1;

    x.resize(count);
    y.resize(count);

    for (uint32_t i = 0; i < count; ++i) {
        FREObject xObject = nullptr;
        FREObject yObject = nullptr;

        if (FREGetArrayElementAt(
                array,
                i * 2u,
                &xObject
            ) != FRE_OK ||
            FREGetArrayElementAt(
                array,
                i * 2u + 1u,
                &yObject
            ) != FRE_OK) {
            return false;
        }

        if (!getInt32(xObject, x[i]) ||
            !getInt32(yObject, y[i])) {
            return false;
        }
    }

    return true;
}

FREObject createQuickSelectTool(
    FREContext ctx,
    void* functionData,
    uint32_t argc,
    FREObject argv[]
)
{
    (void)functionData;

    QuickSelectionContext* s = state(ctx);

    if (s == nullptr || argc != 3) {
        return makeBool(false);
    }

    int width = 0;
    int height = 0;

    if (!getInt32(argv[1], width) ||
        !getInt32(argv[2], height)) {
        return makeBool(false);
    }

    std::size_t pixelCount = 0;

    if (!checkedPixelCount(width, height, pixelCount) ||
        pixelCount >
            std::numeric_limits<std::size_t>::max() / 4u ||
        pixelCount >
            std::numeric_limits<std::size_t>::max() / 3u) {
        return makeBool(false);
    }

    FREByteArray input{};

    if (FREAcquireByteArray(argv[0], &input) != FRE_OK) {
        return makeBool(false);
    }

    const std::size_t requiredInput = pixelCount * 4u;

    if (input.bytes == nullptr ||
        static_cast<std::size_t>(input.length) < requiredInput) {
        FREReleaseByteArray(argv[0]);
        return makeBool(false);
    }

    // Exact TTQuickSelectToolManager conversion:
    //
    // input[i*4 + 0] ignored
    // input[i*4 + 1] -> R plane
    // input[i*4 + 2] -> G plane
    // input[i*4 + 3] -> B plane
    std::vector<uint8_t> planarRGB(pixelCount * 3u);

    for (std::size_t i = 0; i < pixelCount; ++i) {
        planarRGB[i] =
            input.bytes[i * 4u + 1u];

        planarRGB[pixelCount + i] =
            input.bytes[i * 4u + 2u];

        planarRGB[pixelCount * 2u + i] =
            input.bytes[i * 4u + 3u];
    }

    FREReleaseByteArray(argv[0]);

    try {
        s->tool = std::make_unique<QuickSelectTool>(
            planarRGB.data(),
            width,
            height
        );
    }
    catch (...) {
        s->tool.reset();
        return makeBool(false);
    }

    return makeBool(true);
}

FREObject disposeQuickSelectTool(
    FREContext ctx,
    void* functionData,
    uint32_t argc,
    FREObject argv[]
)
{
    (void)functionData;
    (void)argv;

    QuickSelectionContext* s = state(ctx);

    if (s == nullptr || argc != 0) {
        return makeBool(false);
    }

    s->tool.reset();
    return makeBool(true);
}

FREObject setMask(
    FREContext ctx,
    void* functionData,
    uint32_t argc,
    FREObject argv[]
)
{
    (void)functionData;

    QuickSelectionContext* s = state(ctx);

    if (s == nullptr ||
        s->tool == nullptr ||
        argc != 1) {
        return makeBool(false);
    }

    std::size_t count = 0;

    if (!checkedPixelCount(
            s->tool->width(),
            s->tool->height(),
            count)) {
        return makeBool(false);
    }

    FREByteArray input{};

    if (FREAcquireByteArray(argv[0], &input) != FRE_OK) {
        return makeBool(false);
    }

    if (input.bytes == nullptr ||
        static_cast<std::size_t>(input.length) < count) {
        FREReleaseByteArray(argv[0]);
        return makeBool(false);
    }

    s->tool->setCurrentMask(input.bytes, count);

    FREReleaseByteArray(argv[0]);
    return makeBool(true);
}

FREObject setMaskWithARGB(
    FREContext ctx,
    void* functionData,
    uint32_t argc,
    FREObject argv[]
)
{
    (void)functionData;

    QuickSelectionContext* s = state(ctx);

    if (s == nullptr ||
        s->tool == nullptr ||
        argc != 2) {
        return makeBool(false);
    }

    uint32_t requestedCount = 0;

    if (!getUint32(argv[1], requestedCount)) {
        return makeBool(false);
    }

    std::size_t toolCount = 0;

    if (!checkedPixelCount(
            s->tool->width(),
            s->tool->height(),
            toolCount)) {
        return makeBool(false);
    }

    if (static_cast<std::size_t>(requestedCount) != toolCount ||
        static_cast<std::size_t>(requestedCount) >
            std::numeric_limits<std::size_t>::max() / 4u) {
        return makeBool(false);
    }

    FREByteArray input{};

    if (FREAcquireByteArray(argv[0], &input) != FRE_OK) {
        return makeBool(false);
    }

    const std::size_t required =
        static_cast<std::size_t>(requestedCount) * 4u;

    if (input.bytes == nullptr ||
        static_cast<std::size_t>(input.length) < required) {
        FREReleaseByteArray(argv[0]);
        return makeBool(false);
    }

    std::vector<uint8_t> mask(requestedCount);

    for (std::size_t i = 0;
         i < static_cast<std::size_t>(requestedCount);
         ++i) {
        mask[i] =
            input.bytes[i * 4u] > 0x80u
                ? 0xFFu
                : 0x00u;
    }

    FREReleaseByteArray(argv[0]);

    s->tool->setCurrentMask(mask.data(), mask.size());
    return makeBool(true);
}

FREObject brushHit(
    FREContext ctx,
    void* functionData,
    uint32_t argc,
    FREObject argv[]
)
{
    (void)functionData;

    QuickSelectionContext* s = state(ctx);

    if (s == nullptr ||
        s->tool == nullptr ||
        argc != 5) {
        return nullptr;
    }

    int x = 0;
    int y = 0;
    int radius = 0;
    int mode = 0;
    bool getResult = false;

    if (!getInt32(argv[0], x) ||
        !getInt32(argv[1], y) ||
        !getInt32(argv[2], radius) ||
        !getInt32(argv[3], mode) ||
        !getBool(argv[4], getResult)) {
        return nullptr;
    }

    s->tool->brushHit(x, y, radius, mode);

    if (!getResult) {
        return nullptr;
    }

    return makeMaskResult(
        *s->tool,
        s->tool->getMask(),
        0
    );
}

FREObject brushHits(
    FREContext ctx,
    void* functionData,
    uint32_t argc,
    FREObject argv[]
)
{
    (void)functionData;

    QuickSelectionContext* s = state(ctx);

    if (s == nullptr ||
        s->tool == nullptr ||
        argc != 4) {
        return nullptr;
    }

    int mode = 0;
    int radius = 0;
    bool getResult = false;

    if (!getInt32(argv[1], mode) ||
        !getInt32(argv[2], radius) ||
        !getBool(argv[3], getResult)) {
        return nullptr;
    }

    std::vector<int> x;
    std::vector<int> y;

    if (!decodePoints(argv[0], x, y)) {
        return nullptr;
    }

    if (x.size() >
        static_cast<std::size_t>(
            std::numeric_limits<int>::max())) {
        return nullptr;
    }

    const int count = static_cast<int>(x.size());

    std::vector<int> radii(x.size(), radius);
    std::vector<int> modes(x.size(), mode);

    s->tool->brushHits(
        x.data(),
        y.data(),
        radii.data(),
        modes.data(),
        count
    );

    if (!getResult) {
        return nullptr;
    }

    return makeMaskResult(
        *s->tool,
        s->tool->getMask(),
        0
    );
}

FREObject qsBrushHits(
    FREContext ctx,
    void* functionData,
    uint32_t argc,
    FREObject argv[]
)
{
    (void)functionData;

    QuickSelectionContext* s = state(ctx);

    if (s == nullptr ||
        s->tool == nullptr ||
        argc != 4) {
        return nullptr;
    }

    int mode = 0;
    int radius = 0;
    bool getResult = false;

    if (!getInt32(argv[1], mode) ||
        !getInt32(argv[2], radius) ||
        !getBool(argv[3], getResult)) {
        return nullptr;
    }

    std::vector<int> x;
    std::vector<int> y;

    if (!decodePoints(argv[0], x, y)) {
        return nullptr;
    }

    if (x.size() >
        static_cast<std::size_t>(
            std::numeric_limits<int>::max())) {
        return nullptr;
    }

    const int count = static_cast<int>(x.size());
    std::vector<int> radii(x.size(), radius);

    s->tool->qsBrushHits(
        x.data(),
        y.data(),
        radii.data(),
        mode,
        count
    );

    if (!getResult) {
        return nullptr;
    }

    return makeMaskResult(
        *s->tool,
        s->tool->getMask(),
        0
    );
}

FREObject getMask(
    FREContext ctx,
    void* functionData,
    uint32_t argc,
    FREObject argv[]
)
{
    (void)functionData;

    QuickSelectionContext* s = state(ctx);

    if (s == nullptr ||
        s->tool == nullptr ||
        argc != 1) {
        return nullptr;
    }

    uint32_t format = 0;

    if (!getUint32(argv[0], format)) {
        return nullptr;
    }

    return makeMaskResult(
        *s->tool,
        s->tool->getMask(),
        format
    );
}

FREObject getFreshCutMismatchCount(
    FREContext ctx,
    void* functionData,
    uint32_t argc,
    FREObject argv[]
)
{
    (void)functionData;
    (void)argv;

    QuickSelectionContext* s = state(ctx);

    if (s == nullptr ||
        s->tool == nullptr ||
        argc != 0) {
        return nullptr;
    }

    FREObject result = nullptr;

    if (FRENewObjectFromInt32(
            static_cast<int32_t>(
                s->tool->freshCutMismatchCount()
            ),
            &result
        ) != FRE_OK) {
        return nullptr;
    }

    return result;
}

FREObject getRefinementChangedPixelCount(
    FREContext ctx,
    void* functionData,
    uint32_t argc,
    FREObject argv[]
)
{
    (void)functionData;
    (void)argv;

    QuickSelectionContext* s = state(ctx);

    if (s == nullptr ||
        s->tool == nullptr ||
        argc != 0) {
        return nullptr;
    }

    FREObject result = nullptr;

    if (FRENewObjectFromInt32(
            static_cast<int32_t>(
                s->tool->refinementChangedPixelCount()
            ),
            &result
        ) != FRE_OK) {
        return nullptr;
    }

    return result;
}

FREObject getDiagnosticSelfTestStatus(
    FREContext ctx,
    void* functionData,
    uint32_t argc,
    FREObject argv[]
)
{
    (void)ctx;
    (void)functionData;
    (void)argv;

    if (argc != 0) {
        return nullptr;
    }

    std::string status;

    if (adobeBKSelfTestPassed()) {
        status = "PASS";
    } else {
        status = "FAIL: ";

        const char* failure = adobeBKSelfTestFailure();
        status +=
            (failure != nullptr && failure[0] != '\0')
                ? failure
                : "unknown exception";
    }

    FREObject result = nullptr;

    if (FRENewObjectFromUTF8(
            static_cast<uint32_t>(status.size()),
            reinterpret_cast<const uint8_t*>(status.c_str()),
            &result
        ) != FRE_OK) {
        return nullptr;
    }

    return result;
}

FREObject getDiagnosticTerminalCapacities(
    FREContext ctx,
    void* functionData,
    uint32_t argc,
    FREObject argv[]
)
{
    (void)functionData;
    (void)argv;

    QuickSelectionContext* s = state(ctx);

    if (s == nullptr ||
        s->tool == nullptr ||
        argc != 0) {
        return nullptr;
    }

    const auto& source =
        s->tool->diagnosticSourceCapacities();

    const auto& sink =
        s->tool->diagnosticSinkCapacities();

    if (source.size() != sink.size()) {
        return nullptr;
    }

    std::vector<float> values;
    values.reserve(source.size() * 2u);

    for (std::size_t i = 0; i < source.size(); ++i) {
        values.push_back(source[i]);
        values.push_back(sink[i]);
    }

    if (values.empty()) {
        return nullptr;
    }

    return makeByteArray(
        reinterpret_cast<const uint8_t*>(values.data()),
        values.size() * sizeof(float)
    );
}

FREObject getDiagnosticRequestedTerminalCapacities(
    FREContext ctx,
    void* functionData,
    uint32_t argc,
    FREObject argv[]
)
{
    (void)functionData;
    (void)argv;

    QuickSelectionContext* s = state(ctx);

    if (s == nullptr ||
        s->tool == nullptr ||
        argc != 0) {
        return nullptr;
    }

    const auto& source =
        s->tool->diagnosticRequestedSourceCapacities();

    const auto& sink =
        s->tool->diagnosticRequestedSinkCapacities();

    if (source.size() != sink.size()) {
        return nullptr;
    }

    std::vector<float> values;
    values.reserve(source.size() * 2u);

    for (std::size_t i = 0; i < source.size(); ++i) {
        values.push_back(source[i]);
        values.push_back(sink[i]);
    }

    if (values.empty()) {
        return nullptr;
    }

    return makeByteArray(
        reinterpret_cast<const uint8_t*>(values.data()),
        values.size() * sizeof(float)
    );
}

FREObject getDiagnosticUpdateTweightsCapacities(
    FREContext ctx,
    void* functionData,
    uint32_t argc,
    FREObject argv[]
)
{
    (void)functionData;
    (void)argv;

    QuickSelectionContext* s = state(ctx);

    if (s == nullptr ||
        s->tool == nullptr ||
        argc != 0) {
        return nullptr;
    }

    const auto& beforeSource =
        s->tool->diagnosticBeforeUpdateSourceCapacities();

    const auto& beforeSourceResidual =
        s->tool->diagnosticBeforeUpdateSourceResiduals();

    const auto& beforeSink =
        s->tool->diagnosticBeforeUpdateSinkCapacities();

    const auto& beforeSinkResidual =
        s->tool->diagnosticBeforeUpdateSinkResiduals();

    const auto& afterSource =
        s->tool->diagnosticAfterUpdateSourceCapacities();

    const auto& afterSourceResidual =
        s->tool->diagnosticAfterUpdateSourceResiduals();

    const auto& afterSink =
        s->tool->diagnosticAfterUpdateSinkCapacities();

    const auto& afterSinkResidual =
        s->tool->diagnosticAfterUpdateSinkResiduals();

    const std::size_t count = beforeSource.size();

    if (beforeSourceResidual.size() != count ||
        beforeSink.size() != count ||
        beforeSinkResidual.size() != count ||
        afterSource.size() != count ||
        afterSourceResidual.size() != count ||
        afterSink.size() != count ||
        afterSinkResidual.size() != count) {
        return nullptr;
    }

    std::vector<float> values;
    values.reserve(count * 8u);

    for (std::size_t i = 0; i < count; ++i) {
        values.push_back(beforeSource[i]);
        values.push_back(beforeSourceResidual[i]);
        values.push_back(beforeSink[i]);
        values.push_back(beforeSinkResidual[i]);

        values.push_back(afterSource[i]);
        values.push_back(afterSourceResidual[i]);
        values.push_back(afterSink[i]);
        values.push_back(afterSinkResidual[i]);
    }

    if (values.empty()) {
        return nullptr;
    }

    return makeByteArray(
        reinterpret_cast<const uint8_t*>(values.data()),
        values.size() * sizeof(float)
    );
}

FREObject getDiagnosticHorizontalCapacities(
    FREContext ctx,
    void* functionData,
    uint32_t argc,
    FREObject argv[]
)
{
    (void)functionData;
    (void)argv;

    QuickSelectionContext* s = state(ctx);

    if (s == nullptr ||
        s->tool == nullptr ||
        argc != 0) {
        return nullptr;
    }

    const auto& values =
        s->tool->diagnosticHorizontalCapacities();

    if (values.empty()) {
        return nullptr;
    }

    return makeByteArray(
        reinterpret_cast<const uint8_t*>(values.data()),
        values.size() * sizeof(float)
    );
}

FREObject mouseUp(
    FREContext ctx,
    void* functionData,
    uint32_t argc,
    FREObject argv[]
)
{
    (void)functionData;

    QuickSelectionContext* s = state(ctx);

    if (s == nullptr ||
        s->tool == nullptr ||
        argc != 1) {
        return nullptr;
    }

    uint32_t format = 0;

    if (!getUint32(argv[0], format)) {
        return nullptr;
    }

    return makeMaskResult(
        *s->tool,
        s->tool->mouseUp(),
        format
    );
}

FREObject reset(
    FREContext ctx,
    void* functionData,
    uint32_t argc,
    FREObject argv[]
)
{
    (void)functionData;
    (void)argv;

    QuickSelectionContext* s = state(ctx);

    if (s == nullptr ||
        s->tool == nullptr ||
        argc != 0) {
        return nullptr;
    }

    // Exact ARMv7 wrapper behavior.
    s->tool->reset(0);
    return nullptr;
}

FREObject switchMode(
    FREContext ctx,
    void* functionData,
    uint32_t argc,
    FREObject argv[]
)
{
    (void)functionData;

    QuickSelectionContext* s = state(ctx);

    if (s == nullptr ||
        s->tool == nullptr ||
        argc != 1) {
        return nullptr;
    }

    int mode = 0;

    if (!getInt32(argv[0], mode)) {
        return nullptr;
    }

    s->tool->switchMode(mode);
    return nullptr;
}

const FRENamedFunction kQuickSelectionFunctions[] = {
    {
        reinterpret_cast<const uint8_t*>("createQuickSelectTool"),
        nullptr,
        &createQuickSelectTool
    },
    {
        reinterpret_cast<const uint8_t*>("disposeQuickSelectTool"),
        nullptr,
        &disposeQuickSelectTool
    },
    {
        reinterpret_cast<const uint8_t*>("setMask"),
        nullptr,
        &setMask
    },
    {
        reinterpret_cast<const uint8_t*>("setMaskWithARGB"),
        nullptr,
        &setMaskWithARGB
    },
    {
        reinterpret_cast<const uint8_t*>("brushHit"),
        nullptr,
        &brushHit
    },
    {
        reinterpret_cast<const uint8_t*>("brushHits"),
        nullptr,
        &brushHits
    },
    {
        reinterpret_cast<const uint8_t*>("qsBrushHits"),
        nullptr,
        &qsBrushHits
    },
    {
        reinterpret_cast<const uint8_t*>("getMask"),
        nullptr,
        &getMask
    },
    {
        reinterpret_cast<const uint8_t*>("getFreshCutMismatchCount"),
        nullptr,
        &getFreshCutMismatchCount
    },
    {
        reinterpret_cast<const uint8_t*>("getRefinementChangedPixelCount"),
        nullptr,
        &getRefinementChangedPixelCount
    },
    {
        reinterpret_cast<const uint8_t*>("getDiagnosticSelfTestStatus"),
        nullptr,
        &getDiagnosticSelfTestStatus
    },
    {
        reinterpret_cast<const uint8_t*>("getDiagnosticTerminalCapacities"),
        nullptr,
        &getDiagnosticTerminalCapacities
    },
    {
        reinterpret_cast<const uint8_t*>("getDiagnosticRequestedTerminalCapacities"),
        nullptr,
        &getDiagnosticRequestedTerminalCapacities
    },
    {
        reinterpret_cast<const uint8_t*>("getDiagnosticUpdateTweightsCapacities"),
        nullptr,
        &getDiagnosticUpdateTweightsCapacities
    },
    {
        reinterpret_cast<const uint8_t*>("getDiagnosticHorizontalCapacities"),
        nullptr,
        &getDiagnosticHorizontalCapacities
    },
    {
        reinterpret_cast<const uint8_t*>("mouseUp"),
        nullptr,
        &mouseUp
    },
    {
        reinterpret_cast<const uint8_t*>("reset"),
        nullptr,
        &reset
    },
    {
        reinterpret_cast<const uint8_t*>("switchMode"),
        nullptr,
        &switchMode
    },
};

FREObject getSystemInfo(
    FREContext ctx,
    void* functionData,
    uint32_t argc,
    FREObject argv[]
)
{
    (void)ctx;
    (void)functionData;

    if (argc != 1 || argv == nullptr) {
        return nullptr;
    }

    int32_t infoKey = 0;

    if (FREGetObjectAsInt32(
            argv[0],
            &infoKey
        ) != FRE_OK) {
        return nullptr;
    }

    // Original TTNEUtils ABI:
    // SYSINFO_HW_MEMORY = 1.
    //
    // The ARMv7 implementation queries CTL_HW/HW_MEMSIZE and
    // returns physical memory in KiB.
    if (infoKey != 1) {
        return nullptr;
    }

    int mib[2] = {
        CTL_HW,
        HW_MEMSIZE
    };

    uint64_t memoryBytes = 0;
    size_t memoryBytesSize = sizeof(memoryBytes);

    if (sysctl(
            mib,
            2,
            &memoryBytes,
            &memoryBytesSize,
            nullptr,
            0
        ) != 0) {
        return nullptr;
    }

    const uint64_t memoryKiB =
        memoryBytes >> 10;

    FREObject result = nullptr;

    if (FRENewObjectFromInt32(
            static_cast<int32_t>(memoryKiB),
            &result
        ) != FRE_OK) {
        return nullptr;
    }

    return result;
}

FREObject glCreateContext(
    FREContext ctx,
    void* functionData,
    uint32_t argc,
    FREObject argv[]
)
{
    (void)functionData;
    (void)argc;
    (void)argv;

    const int32_t status =
        GLBridgeCreateContext(ctx) ? 0 : -1;

    FREObject result = nullptr;

    if (FRENewObjectFromInt32(
            status,
            &result
        ) != FRE_OK) {
        return nullptr;
    }

    return result;
}

FREObject glDestroyContext(
    FREContext ctx,
    void* functionData,
    uint32_t argc,
    FREObject argv[]
)
{
    (void)functionData;
    (void)argc;
    (void)argv;

    GLBridgeDestroyContext(ctx);

    FREObject result = nullptr;

    if (FRENewObjectFromInt32(
            0,
            &result
        ) != FRE_OK) {
        return nullptr;
    }

    return result;
}

FREObject glGetStringInfo(
    FREContext ctx,
    void* functionData,
    uint32_t argc,
    FREObject argv[]
)
{
    (void)functionData;

    if (argc != 1 || argv == nullptr) {
        return nullptr;
    }

    int infoKey = 0;

    if (!getInt32(argv[0], infoKey)) {
        return nullptr;
    }

    std::string value;

    if (!GLBridgeGetStringInfo(
            ctx,
            static_cast<std::int32_t>(infoKey),
            value
        )) {
        return nullptr;
    }

    FREObject result = nullptr;

    if (FRENewObjectFromUTF8(
            static_cast<uint32_t>(value.size()),
            reinterpret_cast<const uint8_t*>(value.c_str()),
            &result
        ) != FRE_OK) {
        return nullptr;
    }

    return result;
}

const FRENamedFunction kGLFunctions[] = {
    {
        reinterpret_cast<const uint8_t*>("createContext"),
        nullptr,
        &glCreateContext
    },
    {
        reinterpret_cast<const uint8_t*>("destroyContext"),
        nullptr,
        &glDestroyContext
    },
    {
        reinterpret_cast<const uint8_t*>("getStringInfo"),
        nullptr,
        &glGetStringInfo
    },
};

const FRENamedFunction kUtilsFunctions[] = {
    {
        reinterpret_cast<const uint8_t*>("getSystemInfo"),
        nullptr,
        &getSystemInfo
    },
};

void quickSelectionContextInitializer(
    void* extData,
    const uint8_t* ctxType,
    FREContext ctx,
    uint32_t* numFunctionsToSet,
    const FRENamedFunction** functionsToSet
)
{
    (void)extData;

    if (numFunctionsToSet == nullptr ||
        functionsToSet == nullptr) {
        return;
    }

    *numFunctionsToSet = 0;
    *functionsToSet = nullptr;

    if (ctxType == nullptr) {
        return;
    }

    const char* contextType =
        reinterpret_cast<const char*>(ctxType);

    if (strcasecmp(contextType, "utils") == 0) {
        *numFunctionsToSet =
            static_cast<uint32_t>(
                sizeof(kUtilsFunctions) /
                sizeof(kUtilsFunctions[0])
            );

        *functionsToSet = kUtilsFunctions;
        return;
    }

    if (strcasecmp(contextType, "gl") == 0) {
        *numFunctionsToSet =
            static_cast<uint32_t>(
                sizeof(kGLFunctions) /
                sizeof(kGLFunctions[0])
            );

        *functionsToSet = kGLFunctions;
        return;
    }

    if (strcasecmp(contextType, "quickselection") != 0) {
        return;
    }

    auto* s = new QuickSelectionContext();

    if (FRESetContextNativeData(ctx, s) != FRE_OK) {
        delete s;
        return;
    }

    *numFunctionsToSet =
        static_cast<uint32_t>(
            sizeof(kQuickSelectionFunctions) /
            sizeof(kQuickSelectionFunctions[0])
        );

    *functionsToSet = kQuickSelectionFunctions;
}

void quickSelectionContextFinalizer(FREContext ctx)
{
    GLBridgeDestroyContextIfPresent(ctx);

    QuickSelectionContext* s = state(ctx);

    if (s == nullptr) {
        return;
    }

    s->tool.reset();
    FRESetContextNativeData(ctx, nullptr);
    delete s;
}

} // namespace

extern "C" {

void TTPixelExtensionInitializer(
    void** extDataToSet,
    FREContextInitializer* ctxInitializerToSet,
    FREContextFinalizer* ctxFinalizerToSet
)
{
    if (extDataToSet != nullptr) {
        *extDataToSet = nullptr;
    }

    if (ctxInitializerToSet != nullptr) {
        *ctxInitializerToSet =
            &quickSelectionContextInitializer;
    }

    if (ctxFinalizerToSet != nullptr) {
        *ctxFinalizerToSet =
            &quickSelectionContextFinalizer;
    }
}

void TTPixelExtensionFinalizer(void* extData)
{
    (void)extData;
}

}
