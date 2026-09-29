#pragma once

#include <cstdint>
#include <string>

using GLBridgeContextKey = void*;

bool GLBridgeCreateContext(GLBridgeContextKey key);
void GLBridgeDestroyContext(GLBridgeContextKey key);
void GLBridgeDestroyContextIfPresent(GLBridgeContextKey key);

bool GLBridgeGetStringInfo(
    GLBridgeContextKey key,
    std::int32_t infoKey,
    std::string& value
);
