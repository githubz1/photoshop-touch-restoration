package com.adobe.ttpixel.editor.processor
{
    public class TTExtensionWrapper extends CABMessageHandler
    {
        public function TTExtensionWrapper()
        {
            com.adobe.ttpixel.editor.processor:TTExtensionWrapper::cInstance.cInstance = local0;
            "return";
        }

        private static function __static_init__():void
        {
            com.adobe.ttpixel.editor.processor:TTExtensionWrapper::cInstance.cInstance = null;
            EXT_MODULE_GL.EXT_MODULE_GL = "gl";
            EXT_MODULE_IMPEXP.EXT_MODULE_IMPEXP = "impexp";
            EXT_MODULE_AM.EXT_MODULE_AM = "am";
            EXT_MODULE_SS.EXT_MODULE_SS = "ss";
            EXT_MODULE_EMAIL.EXT_MODULE_EMAIL = "email";
            EXT_MODULE_UTILS.EXT_MODULE_UTILS = "utils";
            EXT_MODULE_CAMERAROLL.EXT_MODULE_CAMERAROLL = "cameraroll";
            EXT_MODULE_CAMERAUI.EXT_MODULE_CAMERAUI = "cameraui";
            EXT_MODULE_ACTIVITYUI.EXT_MODULE_ACTIVITYUI = "activityui";
            EXT_MODULE_QUICKSELECTION.EXT_MODULE_QUICKSELECTION = "quickselection";
            SYSINFO_HW_MEMORY.SYSINFO_HW_MEMORY = TTNEUtils.SYSINFO_HW_MEMORY;
            SYSINFO_HW_CPU_CORES.SYSINFO_HW_CPU_CORES = TTNEUtils.SYSINFO_HW_CPU_CORES;
            SYSINFO_OS_NAME.SYSINFO_OS_NAME = TTNEUtils.SYSINFO_OS_NAME;
            SYSINFO_OS_VERSION_MAJOR.SYSINFO_OS_VERSION_MAJOR = TTNEUtils.SYSINFO_OS_VERSION_MAJOR;
            SYSINFO_OS_VERSION_MINOR.SYSINFO_OS_VERSION_MINOR = TTNEUtils.SYSINFO_OS_VERSION_MINOR;
            SYSINFO_OS_VERSION_PATCH.SYSINFO_OS_VERSION_PATCH = TTNEUtils.SYSINFO_OS_VERSION_PATCH;
            SYSINFO_DEVICE_MANUFACTURER.SYSINFO_DEVICE_MANUFACTURER = TTNEUtils.SYSINFO_DEVICE_MANUFACTURER;
            SYSINFO_DEVICE_MODEL.SYSINFO_DEVICE_MODEL = TTNEUtils.SYSINFO_DEVICE_MODEL;
            SYSINFO_SCREEN_DENSITY.SYSINFO_SCREEN_DENSITY = TTNEUtils.SYSINFO_SCREEN_DENSITY;
            SYSINFO_SCREEN_DPI_SQUARE.SYSINFO_SCREEN_DPI_SQUARE = TTNEUtils.SYSINFO_SCREEN_DPI_SQUARE;
            OS_NAME_ANDROID.OS_NAME_ANDROID = TTNEUtils.OS_NAME_ANDROID;
            OS_NAME_IOS.OS_NAME_IOS = TTNEUtils.OS_NAME_IOS;
            kECGLResultSuccess.kECGLResultSuccess = TTGL.kECGLResultSuccess;
            kECGLResultError.kECGLResultError = TTGL.kECGLResultError;
            kECGLResultErrorNotImplemented.kECGLResultErrorNotImplemented = TTGL.kECGLResultErrorNotImplemented;
            kECGLResultErrorOutOfMemory.kECGLResultErrorOutOfMemory = TTGL.kECGLResultErrorOutOfMemory;
            kECGLResultErrorCreatingNativeContextFailed.kECGLResultErrorCreatingNativeContextFailed = TTGL.kECGLResultErrorCreatingNativeContextFailed;
            kECGLResultErrorUsingNativeContextFailed.kECGLResultErrorUsingNativeContextFailed = TTGL.kECGLResultErrorUsingNativeContextFailed;
            kECGLResultErrorWrongParam.kECGLResultErrorWrongParam = TTGL.kECGLResultErrorWrongParam;
            kECGLResultErrorOutOfResources.kECGLResultErrorOutOfResources = TTGL.kECGLResultErrorOutOfResources;
            kECGLResultErrorGL.kECGLResultErrorGL = TTGL.kECGLResultErrorGL;
            kECGLResultErrorGLFramebufferIncomplete.kECGLResultErrorGLFramebufferIncomplete = TTGL.kECGLResultErrorGLFramebufferIncomplete;
            kECGLResultErrorGLCreateProgramFailed.kECGLResultErrorGLCreateProgramFailed = TTGL.kECGLResultErrorGLCreateProgramFailed;
            kECGLResultErrorGLCompileShaderFailed.kECGLResultErrorGLCompileShaderFailed = TTGL.kECGLResultErrorGLCompileShaderFailed;
            kECGLResultErrorGLLinkProgramFailed.kECGLResultErrorGLLinkProgramFailed = TTGL.kECGLResultErrorGLLinkProgramFailed;
            kECGLResultErrorGLNoLocation.kECGLResultErrorGLNoLocation = TTGL.kECGLResultErrorGLNoLocation;
            kECGLResultErrorEGL.kECGLResultErrorEGL = TTGL.kECGLResultErrorEGL;
            kECGLResultErrorEGLNoDisplay.kECGLResultErrorEGLNoDisplay = TTGL.kECGLResultErrorEGLNoDisplay;
            kECGLResultErrorEGLInitializeFailed.kECGLResultErrorEGLInitializeFailed = TTGL.kECGLResultErrorEGLInitializeFailed;
            kECGLResultErrorEGLNoConfig.kECGLResultErrorEGLNoConfig = TTGL.kECGLResultErrorEGLNoConfig;
            kECGLResultErrorEGLNoSurface.kECGLResultErrorEGLNoSurface = TTGL.kECGLResultErrorEGLNoSurface;
            kECGLResultErrorEGLNoContext.kECGLResultErrorEGLNoContext = TTGL.kECGLResultErrorEGLNoContext;
            kECGLResultErrorEGLMakeCurrentFailed.kECGLResultErrorEGLMakeCurrentFailed = TTGL.kECGLResultErrorEGLMakeCurrentFailed;
            kECGLResultErrorEGLBindAPIFailed.kECGLResultErrorEGLBindAPIFailed = TTGL.kECGLResultErrorEGLBindAPIFailed;
            kECGLResultErrorJNI.kECGLResultErrorJNI = TTGL.kECGLResultErrorJNI;
            kECGLResultErrorFRE.kECGLResultErrorFRE = TTGL.kECGLResultErrorFRE;
            kECGLResultErrorAS.kECGLResultErrorAS = TTGL.kECGLResultErrorAS;
            kECGLResultErrorASExtensionNotAvailable.kECGLResultErrorASExtensionNotAvailable = TTGL.kECGLResultErrorASExtensionNotAvailable;
            kECGLResultAborted.kECGLResultAborted = TTGL.kECGLResultAborted;
            kECGLShaderTypeVertex.kECGLShaderTypeVertex = TTGL.kECGLShaderTypeVertex;
            kECGLShaderTypeFragment.kECGLShaderTypeFragment = TTGL.kECGLShaderTypeFragment;
            kECGLMinificationNearest.kECGLMinificationNearest = TTGL.kECGLMinificationNearest;
            kECGLMinificationLinear.kECGLMinificationLinear = TTGL.kECGLMinificationLinear;
            kECGLMinificationNearestMipmapNearest.kECGLMinificationNearestMipmapNearest = TTGL.kECGLMinificationNearestMipmapNearest;
            kECGLMinificationLinearMipmapNearest.kECGLMinificationLinearMipmapNearest = TTGL.kECGLMinificationLinearMipmapNearest;
            kECGLMinificationNearestMipmapLinear.kECGLMinificationNearestMipmapLinear = TTGL.kECGLMinificationNearestMipmapLinear;
            kECGLMinificationLinearMipmapLinear.kECGLMinificationLinearMipmapLinear = TTGL.kECGLMinificationLinearMipmapLinear;
            kECGLMagnificationNearest.kECGLMagnificationNearest = TTGL.kECGLMagnificationNearest;
            kECGLMagnificationLinear.kECGLMagnificationLinear = TTGL.kECGLMagnificationLinear;
            kECGLVertexAttributePosition.kECGLVertexAttributePosition = TTGL.kECGLVertexAttributePosition;
            kECGLVertexAttributeTexcoords.kECGLVertexAttributeTexcoords = TTGL.kECGLVertexAttributeTexcoords;
            kECGLVertexAttribute2.kECGLVertexAttribute2 = TTGL.kECGLVertexAttribute2;
            kECGLVertexAttribute3.kECGLVertexAttribute3 = TTGL.kECGLVertexAttribute3;
            kECGLVertexAttribute4.kECGLVertexAttribute4 = TTGL.kECGLVertexAttribute4;
            kECGLVertexAttribute5.kECGLVertexAttribute5 = TTGL.kECGLVertexAttribute5;
            kECGLVertexAttribute6.kECGLVertexAttribute6 = TTGL.kECGLVertexAttribute6;
            kECGLVertexAttribute7.kECGLVertexAttribute7 = TTGL.kECGLVertexAttribute7;
            kECGLDrawModePoints.kECGLDrawModePoints = TTGL.kECGLDrawModePoints;
            kECGLDrawModeLineStrip.kECGLDrawModeLineStrip = TTGL.kECGLDrawModeLineStrip;
            kECGLDrawModeLineLoop.kECGLDrawModeLineLoop = TTGL.kECGLDrawModeLineLoop;
            kECGLDrawModeLines.kECGLDrawModeLines = TTGL.kECGLDrawModeLines;
            kECGLDrawModeTriangleStrip.kECGLDrawModeTriangleStrip = TTGL.kECGLDrawModeTriangleStrip;
            kECGLDrawModeTriangleFan.kECGLDrawModeTriangleFan = TTGL.kECGLDrawModeTriangleFan;
            kECGLDrawModeTriangles.kECGLDrawModeTriangles = TTGL.kECGLDrawModeTriangles;
            kECGLClearModeColor.kECGLClearModeColor = TTGL.kECGLClearModeColor;
            kECGLClearModeDepth.kECGLClearModeDepth = TTGL.kECGLClearModeDepth;
            kECGLClearModeStencil.kECGLClearModeStencil = TTGL.kECGLClearModeStencil;
            kECGLClearModeAll.kECGLClearModeAll = TTGL.kECGLClearModeAll;
            kECGLMatrixDimension_2x2.kECGLMatrixDimension_2x2 = TTGL.kECGLMatrixDimension_2x2;
            kECGLMatrixDimension_3x3.kECGLMatrixDimension_3x3 = TTGL.kECGLMatrixDimension_3x3;
            kECGLMatrixDimension_4x4.kECGLMatrixDimension_4x4 = TTGL.kECGLMatrixDimension_4x4;
            kECGLComponentsPerValue_1.kECGLComponentsPerValue_1 = TTGL.kECGLComponentsPerValue_1;
            kECGLComponentsPerValue_2.kECGLComponentsPerValue_2 = TTGL.kECGLComponentsPerValue_2;
            kECGLComponentsPerValue_3.kECGLComponentsPerValue_3 = TTGL.kECGLComponentsPerValue_3;
            kECGLComponentsPerValue_4.kECGLComponentsPerValue_4 = TTGL.kECGLComponentsPerValue_4;
            kStringInfoKeyEGLVendor.kStringInfoKeyEGLVendor = TTGL.kStringInfoKeyEGLVendor;
            kStringInfoKeyEGLVersion.kStringInfoKeyEGLVersion = TTGL.kStringInfoKeyEGLVersion;
            kStringInfoKeyEGLExtensions.kStringInfoKeyEGLExtensions = TTGL.kStringInfoKeyEGLExtensions;
            kStringInfoKeyGLVendor.kStringInfoKeyGLVendor = TTGL.kStringInfoKeyGLVendor;
            kStringInfoKeyGLRenderer.kStringInfoKeyGLRenderer = TTGL.kStringInfoKeyGLRenderer;
            kStringInfoKeyGLVersion.kStringInfoKeyGLVersion = TTGL.kStringInfoKeyGLVersion;
            kStringInfoKeyGLShadingLanguageVersion.kStringInfoKeyGLShadingLanguageVersion = TTGL.kStringInfoKeyGLShadingLanguageVersion;
            kStringInfoKeyGLExtensions.kStringInfoKeyGLExtensions = TTGL.kStringInfoKeyGLExtensions;
            kQSResultMask.kQSResultMask = TTQuickSelection.kQSResultMask;
            kQSResultARGB.kQSResultARGB = TTQuickSelection.kQSResultARGB;
            kECImpExpStatusLevelProgress.kECImpExpStatusLevelProgress = "progress";
            kECImpExpStatusLevelFinished.kECImpExpStatusLevelFinished = "finished";
            kECGLStatusLevelFinished.kECGLStatusLevelFinished = "finished";
            return;
        }

        public function getExtensionContext():*
        {
            return null;
        }

        public function isAvailable():*
        {
            return false;
        }

        public function createContext():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function destroyContext():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function getLastGLErrorCode():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function getStringInfo():*
        {
            return null;
        }

        public function createFilterEx():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function prepareTextureEmpty():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function prepareTextureFromBitmapData():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function updateTextureFromBitmapData():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function setParamFloat():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function setParamInt():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function setParamFloatMulti():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function setParamIntMulti():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function applyFilter():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function getNumVertices():*
        {
            return 0;
        }

        public function setNumVertices():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function setVertexAttribDataFloat():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function clearVertexAttribData():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function getVertexAttribDataRangeStart():*
        {
            return 0;
        }

        public function getVertexAttribDataRangeCount():*
        {
            return 0;
        }

        public function setVertexAttribDataRange():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function setDrawMode():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function enableVertexAttrib():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function destroyFilter():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function drawArrays():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function readPixels():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function clear():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function setParamFloatMatrix():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function getShaderInfoLog():*
        {
            return "";
        }

        public function executeSequence():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function asyncExecuteSequence():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function asyncJoin():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function asyncInterrupt():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function asyncReadPixels():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function shaderCreate():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function shaderDispose():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function programCreate():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function programDispose():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function programUse():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function AMInit():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function AMRun():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function AMGetProgress():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function AMGetResult():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function AMRelease():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function SSInit():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function SSRun():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function SSGetProgress():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function SSGetResult():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function SSRelease():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function CameraRollBrowseImages():*
        {
            return;
        }

        public function CameraRollRequestThumbnail():*
        {
            return;
        }

        public function CameraRollCancelPendingRequests():*
        {
            return;
        }

        public function CameraRollGetThumbnail():*
        {
            return null;
        }

        public function CameraRollGetImage():*
        {
            return null;
        }

        public function CameraRollGetExifHeader():*
        {
            return null;
        }

        public function CameraRollRequestImage():*
        {
            return;
        }

        public function CameraRollPickImage():*
        {
            return;
        }

        public function CameraRollGetPickedImage():*
        {
            return null;
        }

        public function CameraRollAccessCheck():*
        {
            return;
        }

        public function CameraUILaunch():*
        {
            return;
        }

        public function CameraUIGetImage():*
        {
            return null;
        }

        public function MailSendMail():*
        {
            return false;
        }

        public function MailSaveFile():*
        {
            return "";
        }

        public function MailCopyFile():*
        {
            return "";
        }

        public function MailReleaseFile():*
        {
            return;
        }

        public function traceMessage():*
        {
            return 0;
        }

        public function getDeviceID():*
        {
            return "";
        }

        public function getDeviceName():*
        {
            return "NOT A MOBILE DEVICE";
        }

        public function canLaunchApp():*
        {
            return false;
        }

        public function launchApp():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function setStatusBarHidden():*
        {
            return;
        }

        public function isolateColor():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function clipboardHasFormat():*
        {
            return false;
        }

        public function clipboardSetFormatData():*
        {
            return;
        }

        public function clipboardSetMultiData():*
        {
            return;
        }

        public function clipboardGetFormatData():*
        {
            return null;
        }

        public function plistToXMLString():*
        {
            return null;
        }

        public function getMemoryStats():*
        {
            return "";
        }

        public function startEncodeLz4():*
        {
            return false;
        }

        public function startEncodeZLib():*
        {
            return false;
        }

        public function startEncodeJPEG():*
        {
            return false;
        }

        public function startEncodePNG():*
        {
            return false;
        }

        public function getEncodingProgress():*
        {
            return 0;
        }

        public function getEncodedDataSize():*
        {
            return 0;
        }

        public function getEncodedData():*
        {
            return false;
        }

        public function requestCancel():*
        {
            return;
        }

        public function hasFinishedEncoding():*
        {
            return false;
        }

        public function waitFinishedEncoding():*
        {
            return kECGLResultErrorASExtensionNotAvailable;
        }

        public function clearEncodedData():*
        {
            return false;
        }

        public function compressBitmapRLE():*
        {
            return 255;
        }

        public function getPixelsRaw():*
        {
            return local1.getPixels(local2);
        }

        public function setPixelsRaw():*
        {
            local1.setPixels(local2, local3);
            return;
        }

        public function getPixels():*
        {
            return local1.getPixels(local2);
        }

        public function setPixels():*
        {
            local1.setPixels(local2, local3);
            return;
        }

        public function getPixelsBitmap():*
        {
            if (!!local4)
            {
            }
            local3.copyPixels(local1, local2, local4);
            return;
        }

        public function lz4Compress():*
        {
            return null;
        }

        public function lz4Uncompress():*
        {
            return false;
        }

        public function lz4Deflate():*
        {
            return false;
        }

        public function lz4Inflate():*
        {
            return false;
        }

        public function bitmapFileCreateEmpty():*
        {
            local5 = new flash.filesystem::File.File(local1);
            if (!!new flash.filesystem::File.File(local1))
            {
                local6 = new flash.filesystem::FileStream.FileStream();
                local6.open(local5, FileMode.WRITE);
                local6.writeInt(local2);
                local6.writeInt(local3);
                local6.close();
                return true;
            }
            return false;
        }

        public function bitmapFileCreateFromBitmapData():*
        {
            if (!local2)
            {
                return false;
                local3 = new flash.filesystem::File.File(local1);
                local4 = new flash.filesystem::FileStream.FileStream();
                (local2.width > 0);
                local5 = TTExtensionWrapper.instance.getPixelsRaw(local2, new flash.geom::Rectangle.Rectangle(0, 0, local2.width, local2.height));
                local4.open(local3, FileMode.WRITE);
                local4.writeInt(local2.width);
                local4.writeInt(local2.height);
                local4.writeBytes(local5);
                local5.clear();
                local4.close();
                return true;
            }
            return false;
        }

        public function bitmapFileWrite():*
        {
            return local0.bitmapFileCreateFromBitmapData(local1, local2);
        }

        public function bitmapFileRead():*
        {
            local5 = new flash.filesystem::File.File(local1);
            if (!(new flash.filesystem::File.File(local1) != null))
            {
                return false;
                return false;
                local7 = new flash.filesystem::FileStream.FileStream();
                local7.open(local5, FileMode.READ);
                local9 = new flash.geom::Rectangle.Rectangle();
                new flash.geom::Rectangle.Rectangle().x = 0;
                local9.y = 0;
                local9.width = local7.readInt();
                local9.height = local7.readInt();
                (local9.width > 0);
                local8 = new flash.utils::ByteArray.ByteArray();
                local7.readBytes(local8);
                local7.close();
                TTExtensionWrapper.instance.setPixelsRaw(local2, local9, local8);
                local8.clear();
            }
            return true;
        }

        public function bitmapFileResample():*
        {
            local5 = new flash.filesystem::File.File(local1);
            if (!(new flash.filesystem::File.File(local1) != null))
            {
                return false;
                return false;
                local7 = new flash.filesystem::FileStream.FileStream();
                local7.open(local5, FileMode.READ);
                local10 = new flash.geom::Rectangle.Rectangle();
                new flash.geom::Rectangle.Rectangle().x = 0;
                local10.y = 0;
                local10.width = local7.readInt();
                local10.height = local7.readInt();
                (local10.width > 0);
                local8 = new flash.utils::ByteArray.ByteArray();
                local7.readBytes(local8);
                local7.close();
                local11 = new flash.display::BitmapData.BitmapData(local10.width, local10.height);
                new flash.display::BitmapData.BitmapData(local10.width, local10.height).setPixels(local10, local8);
                local8.clear();
                local9 = local0.bitmapDataResample(local11, local2, local3, local4);
                local11.dispose();
            }
            return local9;
        }

        public function bitmapDataCopy():*
        {
            return false;
        }

        public function bitmapDataResample():*
        {
            local5 = (local4.width / local3.width);
            local6 = new flash.geom::Matrix.Matrix();
            new flash.geom::Matrix.Matrix().translate(-local3.left, -local3.top);
            local6.scale(local5, local5);
            local6.translate(local4.x, local4.y);
            local7 = new flash.display::Bitmap.Bitmap(local1);
            local2.draw(local7, local6, null, null, null);
            local7.bitmapData = null;
            return true;
        }

        public function uncompressBitmapData():*
        {
            local5 = new flash.display::BitmapData.BitmapData(local2, local3, true, 0);
            local1.uncompress();
            local0.setPixels(local5, local4, local1);
            <Slot> = local0;
            return local5;
        }

        public function bitmapDataToFile():*
        {
            if (!local1)
            {
                return false;
                local4 = new flash.filesystem::File.File(local3);
                local5 = new flash.filesystem::FileStream.FileStream();
                (local2.width > 0);
                local6 = TTExtensionWrapper.instance.getPixelsRaw(local1, local2);
                local5.open(local4, FileMode.WRITE);
                local5.writeInt(local1.width);
                local5.writeInt(local1.height);
                local5.writeInt(local2.x);
                local5.writeInt(local2.y);
                local5.writeInt(local2.width);
                local5.writeInt(local2.height);
                local5.writeBytes(local6);
                local6.clear();
                local5.close();
                return true;
            }
            return false;
        }

        public function bitmapDataFromFile():*
        {
            local2 = new flash.filesystem::File.File(local1);
            if (!(local2 != null))
            {
                return null;
                return null;
                local4 = new flash.filesystem::FileStream.FileStream();
                local4.open(local2, FileMode.READ);
                local6 = local4.readInt();
                local7 = local4.readInt();
                local8 = new flash.geom::Rectangle.Rectangle();
                new flash.geom::Rectangle.Rectangle().x = local4.readInt();
                local8.y = local4.readInt();
                local8.width = local4.readInt();
                local8.height = local4.readInt();
                (local8.width > 0);
                local5 = new flash.utils::ByteArray.ByteArray();
                local4.readBytes(local5);
                local4.close();
                local3 = new flash.display::BitmapData.BitmapData(local6, local7, true, 0);
                TTExtensionWrapper.instance.setPixelsRaw(local3, local8, local5);
                local5.clear();
            }
            return local3;
        }

        public function getSystemInfo():*
        {
            return null;
        }

        public function activityUIShow():*
        {
            return false;
        }

        public function activityUIHide():*
        {
            return false;
        }

        public function alphaBlend():*
        {
            return false;
        }

        public function getTimestamp():*
        {
            return new Date.Date().time;
        }

        public function startNetworkActivityIndicator():*
        {
            return;
        }

        public function hideNetworkActivityIndicator():*
        {
            return;
        }

        public function QSCreateQuickSelectTool():*
        {
            return false;
        }

        public function QSDisposeQuickSelectTool():*
        {
            return false;
        }

        public function QSSetMask():*
        {
            return false;
        }

        public function QSSetMaskWithARGB():*
        {
            return false;
        }

        public function QSBrushHit():*
        {
            return null;
        }

        public function QSBrushHits():*
        {
            return null;
        }

        public function QSBrushHits2():*
        {
            return null;
        }

        public function QSGetMask():*
        {
            return null;
        }

        public function QSMouseUp():*
        {
            return null;
        }

        public function QSReset():*
        {
            return;
        }

        public function QSSwitchMode():*
        {
            return;
        }

        public static function get instance():*
        {
            if (!(com.adobe.ttpixel.editor.processor:TTExtensionWrapper::cInstance != null))
            {
                com.adobe.ttpixel.editor.processor:TTExtensionWrapper::cInstance.cInstance = local0;
            }
            return com.adobe.ttpixel.editor.processor:TTExtensionWrapper::cInstance;
        }
    }
}
