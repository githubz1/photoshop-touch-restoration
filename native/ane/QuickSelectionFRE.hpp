#pragma once

#include "FlashRuntimeExtensions.h"

extern "C" {

void TTPixelExtensionInitializer(
    void** extDataToSet,
    FREContextInitializer* ctxInitializerToSet,
    FREContextFinalizer* ctxFinalizerToSet
);

void TTPixelExtensionFinalizer(void* extData);

}
