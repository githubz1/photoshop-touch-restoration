#include "GLContextBridge.hpp"

#import <OpenGLES/EAGL.h>
#import <OpenGLES/ES2/gl.h>

#include <mutex>
#include <unordered_map>

namespace {

std::mutex gContextMutex;

std::unordered_map<GLBridgeContextKey, EAGLContext*>
    gContexts;

} // namespace

bool GLBridgeCreateContext(GLBridgeContextKey key)
{
    if (key == nullptr) {
        return false;
    }

    std::lock_guard<std::mutex> lock(gContextMutex);

    auto existing = gContexts.find(key);
    if (existing != gContexts.end()) {
        return existing->second != nil;
    }

    EAGLContext* context =
        [[EAGLContext alloc]
            initWithAPI:kEAGLRenderingAPIOpenGLES2];

    if (context == nil) {
        return false;
    }

    gContexts.emplace(key, context);
    return true;
}

void GLBridgeDestroyContext(GLBridgeContextKey key)
{
    if (key == nullptr) {
        return;
    }

    std::lock_guard<std::mutex> lock(gContextMutex);

    auto it = gContexts.find(key);
    if (it == gContexts.end()) {
        return;
    }

    EAGLContext* context = it->second;

    if ([EAGLContext currentContext] == context) {
        [EAGLContext setCurrentContext:nil];
    }

    gContexts.erase(it);
}

void GLBridgeDestroyContextIfPresent(GLBridgeContextKey key)
{
    GLBridgeDestroyContext(key);
}

bool GLBridgeGetStringInfo(
    GLBridgeContextKey key,
    std::int32_t infoKey,
    std::string& value
)
{
    if (key == nullptr) {
        return false;
    }

    value.clear();

    // Proven Photoshop Touch startup key:
    // kStringInfoKeyGLRenderer == 5.
    if (infoKey != 5) {
        return false;
    }

    std::lock_guard<std::mutex> lock(gContextMutex);

    auto it = gContexts.find(key);
    if (it == gContexts.end() || it->second == nil) {
        return false;
    }

    EAGLContext* previous =
        [EAGLContext currentContext];

    if (![EAGLContext setCurrentContext:it->second]) {
        return false;
    }

    const GLubyte* renderer =
        glGetString(GL_RENDERER);

    [EAGLContext setCurrentContext:previous];

    if (renderer == nullptr) {
        return false;
    }

    value =
        reinterpret_cast<const char*>(renderer);

    return true;
}
