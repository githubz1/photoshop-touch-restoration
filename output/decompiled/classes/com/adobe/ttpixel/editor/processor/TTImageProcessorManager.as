package com.adobe.ttpixel.editor.processor
{
    public class TTImageProcessorManager extends CABModule
    {
        public function TTImageProcessorManager()
        {
            if (!!local2.wantsTouch)
            {
                local1.editorView.currentProcessor = local2;
                local1.editorView.updateCursor();
            }
            local1.toolManager.currentTool.imageProcessorStarts();
            local2.startUI(local1, local3);
            local1.editorView.startGPUMode();
            "return";
        }

        private static function __static_init__():void
        {
            local0.iProcessorList = null;
            local0.iProcessorList = null;
            local0.iCreateUndos = true;
            local0.iSpecialDelegate = null;
            local0.iReferenceDispatcher = null;
            com.adobe.ttpixel.editor.processor:TTImageProcessorManager::cInstance.cInstance = local0;
            return;
        }

        public function set createUndos(value:*):void
        {
            com.adobe.cab.core::CABCoreUtils.getObjectForID(iXML.refDispatcher).iReferenceDispatcher = com.adobe.cab.core::CABReferenceDispatcher;
            local0.iProcessorList = new Array();
            local2 = com.adobe.ttpixel.editor.processor::TTImageProcessor;
            local0.iProcessorList.push(local2);
            return;
        }

        public function set specialDelegate(value:*):void
        {
            local4 = local0.iProcessorList;
            return 0;
            return null;
        }

        public function get referenceDispatcher():*
        {
            local3 = local0.iProcessorList;
            0.cleanupModelData();
            return;
        }

        public function get filterInfo():*
        {
            local5 = local0.getProcessorForID(local2);
            if (!local0.getProcessorForID(local2))
            {
                return;
                local1.setupWorkingRect();
                local1.isBigMode;
                local8 = local5.tilingMargin;
                (local1.width <= TTModel.BIG_MODE_DIMENSION);
                local8 = 0;
                local0.iFilterApplyInfo = new Object.Object();
                local0.iFilterApplyInfo.model = local1;
                local0.iFilterApplyInfo.processor = local5;
                local0.iFilterApplyInfo.copyLayer = false;
                local0.iFilterApplyInfo.applySelection = local4;
                local0.iFilterApplyInfo.workingRect = local1.workingRect.clone();
                local0.iFilterApplyInfo.margin = local8;
                local0.iFilterApplyInfo.xIdx = 0;
                local0.iFilterApplyInfo.yIdx = 0;
                local9 = (TTModel.BIG_MODE_DIMENSION - (2 * 0));
                local0.iFilterApplyInfo.xCount = Math.ceil((local1.width / local9));
                local0.iFilterApplyInfo.yCount = Math.ceil((local1.height / local9));
                local0.iFilterApplyInfo.firstCall = true;
                local10 = local1.layerManager.currentLayer.lockBigBitmapData();
                local0.iFilterApplyInfo.sourceBitmap = local10;
                local1.layerManager.workingBitmapData.lock();
                CABFramework.instance.rootContainer.stage.addEventListener(Event.ENTER_FRAME, local0.tileProcessFilter);
                return;
                local1.layerManager.beginPreviewBitmap();
                local6 = local1.layerManager.currentLayer.lockBigBitmapData();
                local5.process(local1, local6, local1.layerManager.workingBitmapData, local1.layerManager.workingBitmapData.rect, local3);
                TTExtensionWrapper.instance.alphaBlend(local6, local1.layerManager.workingBitmapData, local1.selection.bitmapData);
                local5.process(local1, local6, local1.layerManager.workingBitmapData, local1.layerManager.workingBitmapData.rect, local3);
            }
            local1.layerManager.currentLayer.unlockBigBitmapData(false);
            local7 = local1.undoManager.createGroup(local5.getUndoTitle());
            local1.setSimpleLayerBitmap(local1.layerManager.currentLayer, local1.layerManager.workingBitmapData, local1.workingRect, local7);
            local7.submit();
            local1.layerManager.endPreviewBitmap(local1.workingRect);
            local1.editorView.currentProcessor = null;
            return;
        }

        public function get processorList():*
        {
            if (!!local2.wantsTouch)
            {
                local1.editorView.currentProcessor = local2;
                local1.editorView.updateCursor();
            }
            local2.setupBarView();
            local1.modalTopBar = local2.getCustomBarView();
            local0.iReferenceDispatcher.delegate = local2;
            local2.setupMaps(local1);
            local1.layerManager.beginPreviewBitmap(local2.needsShrinkPreview, local2.shrinkSize);
            local1.setupWorkingRect();
            local1.toolManager.currentTool.imageProcessorStarts();
            local2.startUI(local1, local3);
            return;
        }

        public function readXML():*
        {
            local3 = new flash.display::BitmapData.BitmapData(local1.width, local1.height, true, 0);
            TTBaseMolehillHandler.instance.renderFilterToBitmap(local3);
            local4 = local1.undoManager.createGroup(local2.getUndoTitle());
            local1.setSimpleLayerBitmap(local1.layerManager.currentLayer, local3, local3.rect, local4);
            local3.dispose();
            local4.submit();
            local1.editorView.stopGPUMode();
            local1.editorView.currentProcessor = null;
            local1.editorView.updateCursor();
            return;
        }

        public function postCreate():*
        {
            local1.editorView.stopGPUMode();
            local1.editorView.currentProcessor = null;
            local1.editorView.updateCursor();
            return;
        }

        public function getProcessorForID():*
        {
            local1.processor.startTilingProcess(local1);
            return;
        }

        public function cleanupProcessors():*
        {
            local1.processor.processTile(local1);
            return;
        }

        public function applyFilterDirect():*
        {
            local1.processor.endTilingProcess(local1);
            return;
        }

        public function startUIFilter():*
        {
            if (!!local0.iFilterApplyInfo.firstCall)
            {
                com.adobe.cab.core::CABControlMessage.send(local0.iFilterApplyInfo.processor, FILTER_APPLY_START_SELECTOR, local0);
                local0.iFilterApplyInfo.sourceRect = new flash.geom::Rectangle.Rectangle();
                local0.iFilterApplyInfo.textureRect = new flash.geom::Rectangle.Rectangle();
                local0.iFilterApplyInfo.idx = 0;
                local0.iFilterApplyInfo.tileCount = (local0.iFilterApplyInfo.xCount * local0.iFilterApplyInfo.yCount);
                local0.iFilterApplyInfo.firstCall = false;
                local0.calculateSourceRect(local0.iFilterApplyInfo);
                local0.iFilterApplyInfo.tileBitmap = new flash.display::BitmapData.BitmapData(local0.iFilterApplyInfo.sourceRect.width, local0.iFilterApplyInfo.sourceRect.height, true, 0);
                TTApplication.instance.progressStart();
                local0.startProcessTiling(local0.iFilterApplyInfo);
                return;
                local0.calculateSourceRect(local0.iFilterApplyInfo);
                local0.currentProcessTiling(local0.iFilterApplyInfo);
                TTApplication.instance.progressBusy(local0.iFilterApplyInfo.idx, local0.iFilterApplyInfo.tileCount);
                local4 = local0.iFilterApplyInfo;
                local5 = local0.iFilterApplyInfo.idx;
                local4.idx = local5;
                local4 = local0.iFilterApplyInfo;
                local5 = local0.iFilterApplyInfo.xIdx;
                local4.xIdx = local5;
                local0.iFilterApplyInfo.xIdx = 0;
                local4 = local0.iFilterApplyInfo;
                local5 = local0.iFilterApplyInfo.yIdx;
                local4.yIdx = local5;
                local0.iFilterApplyInfo.tileBitmap.dispose();
                TTApplication.instance.progressEnd();
                CABFramework.instance.rootContainer.stage.removeEventListener(Event.ENTER_FRAME, local0.tileProcessFilter);
                local0.endProcessTiling(local0.iFilterApplyInfo);
                local2 = local0.iFilterApplyInfo.model;
                TTExtensionWrapper.instance.alphaBlend(local0.iFilterApplyInfo.sourceBitmap, local2.layerManager.workingBitmapData, local2.selection.bitmapData);
                local3 = local2.undoManager.createGroup(local0.iFilterApplyInfo.processor.getUndoTitle());
                local2.copySimpleLayerBitmap(local2.layerManager.currentLayer, local2.layerManager.workingBitmapData, local2.workingRect, local3);
                local2.setSimpleLayerBitmap(local2.layerManager.currentLayer, local2.layerManager.workingBitmapData, local2.workingRect, local3);
                local2.layerManager.workingBitmapData.fillRect(local2.layerManager.workingBitmapData.rect, 0);
                local2.layerManager.workingBitmapData.unlock();
                local2.layerManager.currentLayer.unlockBigBitmapData(true);
                local3.submit();
                local2.layerManager.workingBitmapData.unlock();
                local2.layerManager.currentLayer.unlockBigBitmapData(true);
                com.adobe.cab.core::CABControlMessage.send(local0.iFilterApplyInfo.processor, FILTER_APPLY_DONE_SELECTOR, local0);
                com.adobe.cab.core::CABControlMessage.send(local0.iSpecialDelegate, FILTER_APPLY_DONE_SELECTOR, local0);
                local0.iFilterApplyInfo.model.editorView.currentProcessor = null;
                local0.iFilterApplyInfo.model.editorView.updateCursor();
                local0.iFilterApplyInfo = null;
            }
            return;
        }

        public function startGPUUIFilter():*
        {
            local2 = TTModel.BIG_MODE_DIMENSION;
            local1.sourceRect.x = (local0.iFilterApplyInfo.xIdx * (local2 - (2 * local1.margin)));
            local1.sourceRect.y = (local0.iFilterApplyInfo.yIdx * (local2 - (2 * local1.margin)));
            local1.sourceRect.width = Math.min(local2, (local1.model.width - local1.sourceRect.x));
            local1.sourceRect.height = Math.min(local2, (local1.model.height - local1.sourceRect.y));
            return;
        }

        public function applyGPUFilter():*
        {
            if (!!local1.bigPreviewMode)
            {
                local1.layerManager.endPreviewBitmap(local1.workingRect);
                local1.isBigMode;
                local5 = local2.tilingMargin;
                (local1.width <= TTModel.BIG_MODE_DIMENSION);
                local5 = 0;
                local0.iFilterApplyInfo = new Object.Object();
                local0.iFilterApplyInfo.model = local1;
                local0.iFilterApplyInfo.processor = local2;
                local0.iFilterApplyInfo.copyLayer = local3;
                local0.iFilterApplyInfo.applySelection = local4;
                local0.iFilterApplyInfo.workingRect = local1.workingRect.clone();
                local0.iFilterApplyInfo.margin = local5;
                local0.iFilterApplyInfo.xIdx = 0;
                local0.iFilterApplyInfo.yIdx = 0;
                local6 = (TTModel.BIG_MODE_DIMENSION - 0);
                local0.iFilterApplyInfo.xCount = Math.ceil((local1.width / local6));
                local0.iFilterApplyInfo.yCount = Math.ceil((local1.height / local6));
                local0.iFilterApplyInfo.firstCall = true;
                local7 = local1.layerManager.currentLayer.lockBigBitmapData();
                local0.iFilterApplyInfo.sourceBitmap = local7;
                local1.layerManager.workingBitmapData.lock();
                CABFramework.instance.rootContainer.stage.addEventListener(Event.ENTER_FRAME, local0.tileProcessFilter);
                return;
                local1.isBigMode;
                local8 = local1.layerManager.currentLayer.lockBigBitmapData();
                local2.process(local1, local8, local1.layerManager.workingBitmapData, local1.workingRect);
                TTExtensionWrapper.instance.alphaBlend(local8, local1.layerManager.workingBitmapData, local1.selection.bitmapData);
                local1.layerManager.currentLayer.unlockBigBitmapData(true);
                local1.layerManager.workingBitmapData.copyPixels(local1.layerManager.workingBitmapData, local1.layerManager.workingBitmapData.rect, new flash.geom::Point.Point(), local1.selection.bitmapData);
                local9 = local1.undoManager.createGroup(local2.getUndoTitle());
                local1.copySimpleLayerBitmap(local1.layerManager.currentLayer, local1.layerManager.workingBitmapData, local1.workingRect, local9);
                local1.setSimpleLayerBitmap(local1.layerManager.currentLayer, local1.layerManager.workingBitmapData, local1.workingRect, local9);
                local9.submit();
                local1.layerManager.endPreviewBitmap(local1.workingRect);
                com.adobe.cab.core::CABControlMessage.send(local1.editorView.currentProcessor, FILTER_APPLY_DONE_SELECTOR, local0);
                com.adobe.cab.core::CABControlMessage.send(local0.iSpecialDelegate, FILTER_APPLY_DONE_SELECTOR, local0);
            }
            local1.editorView.currentProcessor = null;
            local1.editorView.updateCursor();
            return;
        }

        public function cancelGPUFilter():*
        {
            local1.layerManager.endPreviewBitmap(local1.workingRect);
            local1.editorView.currentProcessor = null;
            local1.editorView.updateCursor();
            if (!(local0.iSpecialDelegate == null))
            {
                com.adobe.cab.core::CABControlMessage.send(local0.iSpecialDelegate, "CancelFilter", local2);
            }
            return;
        }

        public function startProcessTiling():*
        {
            local6 = new flash.display::BitmapData.BitmapData(local1, local2, true, 0);
            new flash.display::BitmapData.BitmapData(local1, local2, true, 0).perlinNoise(local3, local4, local5, 127, true, true, 7, true);
            return local6;
        }

        public function currentProcessTiling():*
        {
            return local1;
        }

        public function endProcessTiling():*
        {
            com.adobe.cab.application::CABModule.TTImageProcessorManager = com.adobe.cab.application::CABModule;
            return;
        }

        public function tileProcessFilter():*
        {
            return;
        }

        public function calculateSourceRect():*
        {
            return local0.iVisible;
        }

        public function applyFilter():*
        {
            local0.iVisible = local1;
            local0.iNeedUpdateMatrix = true;
            return;
        }

        public function cancelFilter():*
        {
            return Math.abs((local0.iWidth * local0.scaleX));
        }

        public function createNoiseBitmapData():*
        {
            local0.scaleX = (local1 / local0.iWidth);
            local0.iNeedUpdateMatrix = true;
            return;
        }

        public function controlMessage():*
        {
            return Math.abs((local0.iHeight * local0.scaleY));
        }

        public static function get instance():*
        {
            return;
        }
    }
}
