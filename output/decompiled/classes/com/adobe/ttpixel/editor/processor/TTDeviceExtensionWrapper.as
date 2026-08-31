package com.adobe.ttpixel.editor.processor
{
    public class TTDeviceExtensionWrapper extends TTExtensionWrapper
    {
        public function TTDeviceExtensionWrapper()
        {
            com.adobe.ttpixel.editor.processor:TTExtensionWrapper::cInstance.cInstance = local0;
            "return";
        }

        private static function __static_init__():void
        {
            return;
        }

        public function getExtensionContext():*
        {
            return null;
        }

        public function isAvailable():*
        {
        }

        public function createContext():*
        {
            return com.adobe.ttpixel.extension::TTGL.createContext();
        }

        public function destroyContext():*
        {
            return com.adobe.ttpixel.extension::TTGL.destroyContext();
        }

        public function getLastGLErrorCode():*
        {
            return com.adobe.ttpixel.extension::TTGL.getLastGLErrorCode();
        }

        public function getStringInfo():*
        {
            return com.adobe.ttpixel.extension::TTGL.getStringInfo(local1);
        }

        public function filterShaderSource():*
        {
            local4 = 0;
            local4 = local1.indexOf("SHIFTCOMPS(", local4);
            local5 = local1.indexOf(")", (local4 + 1));
            local7 = local1.substring((local4 + 11), local5);
            local6 = local1.substring((local4 + 11), local5).length;
            local3 = ((local1.substring(0, local4) + "") + local1.substring(((local4 + 12) + local6), local1.length));
            local4 = (local4 + "".length);
            return local3;
        }

        public function createFilterEx():*
        {
            if (!(local3 == null))
            {
                (local3 == null);
                return com.adobe.ttpixel.extension::TTGL.createFilterEx(local1, local2);
            }
            local5 = "\n#define TT_".concat(CABFramework.instance.os).concat("\n");
            local6 = local0.filterShaderSource(local4, true);
            return com.adobe.ttpixel.extension::TTGL.createFilterEx(local1, local2, local5.concat(local3), local5.concat(local6));
        }

        public function prepareTextureEmpty():*
        {
            return com.adobe.ttpixel.extension::TTGL.prepareTextureEmpty(local1, local2, local3, local4, local5);
        }

        public function prepareTextureFromBitmapData():*
        {
            return com.adobe.ttpixel.extension::TTGL.prepareTextureFromBitmapData(local1, local2, local3, local4);
        }

        public function updateTextureFromBitmapData():*
        {
            return com.adobe.ttpixel.extension::TTGL.updateTextureFromBitmapData(local1, local2, local3, local4);
        }

        public function setParamFloat():*
        {
            return com.adobe.ttpixel.extension::TTGL.setParamFloat(local1, local2);
        }

        public function setParamInt():*
        {
            return com.adobe.ttpixel.extension::TTGL.setParamInt(local1, local2);
        }

        public function setParamFloatMulti():*
        {
            return com.adobe.ttpixel.extension::TTGL.setParamFloatMulti(local1, local2, local3);
        }

        public function setParamIntMulti():*
        {
            return com.adobe.ttpixel.extension::TTGL.setParamIntMulti(local1, local2, local3);
        }

        public function applyFilter():*
        {
            return com.adobe.ttpixel.extension::TTGL.applyFilter(local1, local2, local3);
        }

        public function getNumVertices():*
        {
            return com.adobe.ttpixel.extension::TTGL.getNumVertices();
        }

        public function setNumVertices():*
        {
            return com.adobe.ttpixel.extension::TTGL.setNumVertices(local1);
        }

        public function setVertexAttribDataFloat():*
        {
            return com.adobe.ttpixel.extension::TTGL.setVertexAttribDataFloat(local1, local2, local3);
        }

        public function clearVertexAttribData():*
        {
            return com.adobe.ttpixel.extension::TTGL.clearVertexAttribData(local1);
        }

        public function getVertexAttribDataRangeStart():*
        {
            return com.adobe.ttpixel.extension::TTGL.getVertexAttribDataRangeStart();
        }

        public function getVertexAttribDataRangeCount():*
        {
            return com.adobe.ttpixel.extension::TTGL.getVertexAttribDataRangeCount();
        }

        public function setVertexAttribDataRange():*
        {
            return com.adobe.ttpixel.extension::TTGL.setVertexAttribDataRange(local1, local2);
        }

        public function setDrawMode():*
        {
            return com.adobe.ttpixel.extension::TTGL.setDrawMode(local1);
        }

        public function enableVertexAttrib():*
        {
            return com.adobe.ttpixel.extension::TTGL.enableVertexAttrib(local1, local2);
        }

        public function destroyFilter():*
        {
            return com.adobe.ttpixel.extension::TTGL.destroyFilter();
        }

        public function drawArrays():*
        {
            return com.adobe.ttpixel.extension::TTGL.drawArrays(local1, local2);
        }

        public function readPixels():*
        {
            return com.adobe.ttpixel.extension::TTGL.readPixels(local1, local2);
        }

        public function clear():*
        {
            return com.adobe.ttpixel.extension::TTGL.clear(local1);
        }

        public function setParamFloatMatrix():*
        {
            return com.adobe.ttpixel.extension::TTGL.setParamFloatMatrix(local1, local2, local3, local4);
        }

        public function getShaderInfoLog():*
        {
            return com.adobe.ttpixel.extension::TTGL.getShaderInfoLog();
        }

        public function executeSequence():*
        {
            return com.adobe.ttpixel.extension::TTGL.executeSequence(local1);
        }

        public function asyncExecuteSequence():*
        {
            return com.adobe.ttpixel.extension::TTGL.asyncExecuteSequence(local1);
        }

        public function asyncJoin():*
        {
            return com.adobe.ttpixel.extension::TTGL.asyncJoin();
        }

        public function asyncInterrupt():*
        {
            return com.adobe.ttpixel.extension::TTGL.asyncInterrupt();
        }

        public function asyncReadPixels():*
        {
            return com.adobe.ttpixel.extension::TTGL.asyncReadPixels(local1);
        }

        public function shaderCreate():*
        {
            if (!(local2 == kECGLShaderTypeVertex))
            {
                local3 = local0.filterShaderSource(local3, true);
            }
            local3 = "\n#define TT_".concat(CABFramework.instance.os).concat("\n").concat(local3);
            return com.adobe.ttpixel.extension::TTGL.shaderCreate(local1, local2, local3);
        }

        public function shaderDispose():*
        {
            return com.adobe.ttpixel.extension::TTGL.shaderDispose(local1);
        }

        public function programCreate():*
        {
            return com.adobe.ttpixel.extension::TTGL.programCreate(local1, local2, local3);
        }

        public function programDispose():*
        {
            return com.adobe.ttpixel.extension::TTGL.programDispose(local1);
        }

        public function programUse():*
        {
            return com.adobe.ttpixel.extension::TTGL.programUse(local1);
        }

        public function AMInit():*
        {
            return com.adobe.ttpixel.extension::TTAM.init(local1, local2, local3, local4);
        }

        public function AMRun():*
        {
            return com.adobe.ttpixel.extension::TTAM.run();
        }

        public function AMGetProgress():*
        {
            return com.adobe.ttpixel.extension::TTAM.getProgress();
        }

        public function AMGetResult():*
        {
            return com.adobe.ttpixel.extension::TTAM.getResult(local1);
        }

        public function AMRelease():*
        {
            return com.adobe.ttpixel.extension::TTAM.release();
        }

        public function SSInit():*
        {
            return com.adobe.ttpixel.extension::TTSS.init();
        }

        public function SSRun():*
        {
            return com.adobe.ttpixel.extension::TTSS.run(local1, local2, local3, local4, local5, local6);
        }

        public function SSGetProgress():*
        {
            return com.adobe.ttpixel.extension::TTSS.getProgress();
        }

        public function SSGetResult():*
        {
            return com.adobe.ttpixel.extension::TTSS.getResult();
        }

        public function SSRelease():*
        {
            return com.adobe.ttpixel.extension::TTSS.release();
        }

        public function CameraRollBrowseImages():*
        {
            com.adobe.ttpixel.extension::TTCameraRoll.browseImages(local1, local2);
            return;
        }

        public function CameraRollRequestThumbnail():*
        {
            com.adobe.ttpixel.extension::TTCameraRoll.requestThumbnail(local1);
            return;
        }

        public function CameraRollCancelPendingRequests():*
        {
            com.adobe.ttpixel.extension::TTCameraRoll.cancelPendingRequests();
            return;
        }

        public function CameraRollGetThumbnail():*
        {
            return com.adobe.ttpixel.extension::TTCameraRoll.getThumbnail(local1);
        }

        public function CameraRollGetImage():*
        {
            return com.adobe.ttpixel.extension::TTCameraRoll.getImage(local1);
        }

        public function CameraRollGetExifHeader():*
        {
            return com.adobe.ttpixel.extension::TTCameraRoll.getExifHeader(local1, local2);
        }

        public function CameraRollRequestImage():*
        {
            com.adobe.ttpixel.extension::TTCameraRoll.requestImage(local1, local2);
            return;
        }

        public function CameraRollPickImage():*
        {
            com.adobe.ttpixel.extension::TTCameraRoll.pickImage(local1, local2);
            return;
        }

        public function CameraRollGetPickedImage():*
        {
            return com.adobe.ttpixel.extension::TTCameraRoll.getPickedImage();
        }

        public function CameraRollAccessCheck():*
        {
            com.adobe.ttpixel.extension::TTCameraRoll.accessCheck();
            return;
        }

        public function CameraUILaunch():*
        {
            com.adobe.ttpixel.extension::TTCameraUI.launch(local1, local2);
            return;
        }

        public function CameraUIGetImage():*
        {
            return com.adobe.ttpixel.extension::TTCameraUI.getImage();
        }

        public function MailSendMail():*
        {
            return com.adobe.ttpixel.extension::TTMail.sendMail(local1, local2, local3, local4);
        }

        public function MailSaveFile():*
        {
            return com.adobe.ttpixel.extension::TTMail.saveFile(local1, local2, local3, local4);
        }

        public function MailCopyFile():*
        {
            return com.adobe.ttpixel.extension::TTMail.copyFile(local1, local2, local3, local4);
        }

        public function MailReleaseFile():*
        {
            com.adobe.ttpixel.extension::TTMail.releaseFile(local1);
            return;
        }

        public function traceMessage():*
        {
            return com.adobe.ttpixel.extension::TTNEUtils.traceMessage(local1);
        }

        public function getDeviceID():*
        {
            return com.adobe.ttpixel.extension::TTNEUtils.getDeviceID();
        }

        public function getDeviceName():*
        {
            return com.adobe.ttpixel.extension::TTNEUtils.getDeviceName();
        }

        public function canLaunchApp():*
        {
            return com.adobe.ttpixel.extension::TTNEUtils.canLaunchApp(local1, local2);
        }

        public function launchApp():*
        {
            return com.adobe.ttpixel.extension::TTNEUtils.launchApp(local1, local2);
        }

        public function setStatusBarHidden():*
        {
            com.adobe.ttpixel.extension::TTNEUtils.setStatusBarHidden(local1, local2);
            return;
        }

        public function isolateColor():*
        {
            return com.adobe.ttpixel.extension::TTNEUtils.isolateColor(local1, local2, local3, local4, local5, local6, local7);
        }

        public function clipboardHasFormat():*
        {
            return com.adobe.ttpixel.extension::TTNEUtils.clipboardHasFormat(local1, local2);
        }

        public function clipboardSetFormatData():*
        {
            com.adobe.ttpixel.extension::TTNEUtils.clipboardSetFormatData(local1, local2, local3);
            return;
        }

        public function clipboardSetMultiData():*
        {
            com.adobe.ttpixel.extension::TTNEUtils.clipboardSetMultiData(local1, local2, local3, local4);
            return;
        }

        public function clipboardGetFormatData():*
        {
            return com.adobe.ttpixel.extension::TTNEUtils.clipboardGetFormatData(local1, local2);
        }

        public function plistToXMLString():*
        {
            return com.adobe.ttpixel.extension::TTNEUtils.plistToXMLString(local1);
        }

        public function getMemoryStats():*
        {
            return com.adobe.ttpixel.extension::TTNEUtils.getMemoryStats();
        }

        public function startEncodeLz4():*
        {
            return com.adobe.ttpixel.extension::TTImpExp.startEncodeLz4(local1);
        }

        public function startEncodeZLib():*
        {
            return com.adobe.ttpixel.extension::TTImpExp.startEncodeZLib(local1);
        }

        public function startEncodeJPEG():*
        {
            return com.adobe.ttpixel.extension::TTImpExp.startEncodeJPEG(local1, local2, local3, local4);
        }

        public function startEncodePNG():*
        {
            return com.adobe.ttpixel.extension::TTImpExp.startEncodePNG(local1);
        }

        public function getEncodingProgress():*
        {
            return com.adobe.ttpixel.extension::TTImpExp.getEncodingProgress();
        }

        public function getEncodedDataSize():*
        {
            return com.adobe.ttpixel.extension::TTImpExp.getEncodedDataSize();
        }

        public function getEncodedData():*
        {
            return com.adobe.ttpixel.extension::TTImpExp.getEncodedData(local1);
        }

        public function requestCancel():*
        {
            com.adobe.ttpixel.extension::TTImpExp.requestCancel();
            return;
        }

        public function hasFinishedEncoding():*
        {
            return com.adobe.ttpixel.extension::TTImpExp.hasFinishedEncoding();
        }

        public function waitFinishedEncoding():*
        {
            return com.adobe.ttpixel.extension::TTImpExp.waitFinishedEncoding();
        }

        public function clearEncodedData():*
        {
            return com.adobe.ttpixel.extension::TTImpExp.clearEncodedData();
        }

        public function compressBitmapRLE():*
        {
            return com.adobe.ttpixel.extension::TTNEUtils.compressBitmapRLE(local1, local2, local3, local4, local5);
        }

        public function getPixelsRaw():*
        {
            return com.adobe.ttpixel.extension::TTNEUtils.getPixelsEx(local1, local2, null, false);
        }

        public function setPixelsRaw():*
        {
            com.adobe.ttpixel.extension::TTNEUtils.setPixelsEx(local1, local2, local3, false);
            return;
        }

        public function getPixels():*
        {
            return com.adobe.ttpixel.extension::TTNEUtils.getPixelsEx(local1, local2, null, true);
        }

        public function setPixels():*
        {
            com.adobe.ttpixel.extension::TTNEUtils.setPixelsEx(local1, local2, local3, true);
            return;
        }

        public function lz4Compress():*
        {
            return com.adobe.ttpixel.extension::TTNEUtils.lz4Compress(local1);
        }

        public function lz4Uncompress():*
        {
            return com.adobe.ttpixel.extension::TTNEUtils.lz4Uncompress(local1, local2);
        }

        public function lz4Deflate():*
        {
            return com.adobe.ttpixel.extension::TTNEUtils.lz4Deflate(local1);
        }

        public function lz4Inflate():*
        {
            return com.adobe.ttpixel.extension::TTNEUtils.lz4Inflate(local1);
        }

        public function bitmapFileCreateEmpty():*
        {
            return com.adobe.ttpixel.extension::TTNEUtils.bitmapFileCreateEmpty(local1, local2, local3, local4);
        }

        public function bitmapFileCreateFromBitmapData():*
        {
            return com.adobe.ttpixel.extension::TTNEUtils.bitmapFileCreateFromBitmapData(local1, local2);
        }

        public function bitmapFileWrite():*
        {
            return com.adobe.ttpixel.extension::TTNEUtils.bitmapFileWrite(local1, local2, local3, local4);
        }

        public function bitmapFileRead():*
        {
            return com.adobe.ttpixel.extension::TTNEUtils.bitmapFileRead(local1, local2, local3, local4);
        }

        public function bitmapFileResample():*
        {
            return com.adobe.ttpixel.extension::TTNEUtils.bitmapFileResample(local1, local2, local3, local4);
        }

        public function bitmapDataCopy():*
        {
            return com.adobe.ttpixel.extension::TTNEUtils.bitmapDataCopy(local1, local2, local3, local4);
        }

        public function bitmapDataResample():*
        {
            return com.adobe.ttpixel.extension::TTNEUtils.bitmapDataResample(local1, local2, local3, local4);
        }

        public function getPixelsBitmap():*
        {
            com.adobe.ttpixel.extension::TTNEUtils.getPixelsBitmapEx(local1, local2, local3, local4, true);
            return;
        }

        public function uncompressBitmapData():*
        {
            return com.adobe.ttpixel.extension::TTNEUtils.uncompressBitmapData(local1, local2, local3, local4);
        }

        public function bitmapDataToFile():*
        {
            return com.adobe.ttpixel.extension::TTNEUtils.bitmapDataToFile(local1, local2, local3);
        }

        public function bitmapDataFromFile():*
        {
            return com.adobe.ttpixel.extension::TTNEUtils.bitmapDataFromFile(local1);
        }

        public function getSystemInfo():*
        {
            return com.adobe.ttpixel.extension::TTNEUtils.getSystemInfo(local1);
        }

        public function activityUIShow():*
        {
            return com.adobe.ttpixel.extension::TTActivityUI.showActivityUI(local1, local2, local3, local4, local5);
        }

        public function activityUIHide():*
        {
            return com.adobe.ttpixel.extension::TTActivityUI.hideActivityUI(local1);
        }

        public function alphaBlend():*
        {
            return com.adobe.ttpixel.extension::TTNEUtils.alphaBlend(local1, local2, local3, local4, local5, local6);
        }

        public function getTimestamp():*
        {
            return com.adobe.ttpixel.extension::TTNEUtils.getTimestamp();
        }

        public function startNetworkActivityIndicator():*
        {
            com.adobe.ttpixel.extension::TTNEUtils.startNetworkActivityIndicator();
            return;
        }

        public function hideNetworkActivityIndicator():*
        {
            com.adobe.ttpixel.extension::TTNEUtils.hideNetworkActivityIndicator(local1);
            return;
        }

        public function QSCreateQuickSelectTool():*
        {
            return com.adobe.ttpixel.extension::TTQuickSelection.createQuickSelectTool(local1, local2, local3);
        }

        public function QSDisposeQuickSelectTool():*
        {
            return com.adobe.ttpixel.extension::TTQuickSelection.disposeQuickSelectTool();
        }

        public function QSSetMask():*
        {
            return com.adobe.ttpixel.extension::TTQuickSelection.setMask(local1);
        }

        public function QSSetMaskWithARGB():*
        {
            return com.adobe.ttpixel.extension::TTQuickSelection.setMaskWithARGB(local1, local2);
        }

        public function QSBrushHit():*
        {
            return com.adobe.ttpixel.extension::TTQuickSelection.brushHit(local1, local2, local3, local4, local5);
        }

        public function QSBrushHits():*
        {
            return com.adobe.ttpixel.extension::TTQuickSelection.brushHits(local1, local2, local3, local4);
        }

        public function QSBrushHits2():*
        {
            return com.adobe.ttpixel.extension::TTQuickSelection.qsBrushHits(local1, local2, local3, local4);
        }

        public function QSGetMask():*
        {
            return com.adobe.ttpixel.extension::TTQuickSelection.getMask(local1);
        }

        public function QSMouseUp():*
        {
            return com.adobe.ttpixel.extension::TTQuickSelection.mouseUp(local1);
        }

        public function QSReset():*
        {
            com.adobe.ttpixel.extension::TTQuickSelection.reset();
            return;
        }

        public function QSSwitchMode():*
        {
            com.adobe.ttpixel.extension::TTQuickSelection.switchMode(local1);
            return;
        }
    }
}
