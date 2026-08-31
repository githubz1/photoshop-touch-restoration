// Decompiled by Phoenix

function method1961()
{
    local5 = 0;
    if ()
    {
        local1 = local5;
        local2 = local1;
        !(0 == "");
        local3 = com.adobe.cab.net::CABHTTPClient.createMethod(CABHTTPMethod.TYPE_GET, local2);
        local3.dataFormat = CABHTTPMethod.DATAFORMAT_BINARY;
        local0.openModelWithMethod(local3, "");
        TTOmnitureWrapper.instance.trackAppSpecific("Import: Document");
        local0.iGallery.loadImageFromURL(local2);
        TTOmnitureWrapper.instance.trackAppSpecific("Import: File");
        TTOmnitureWrapper.instance.trackAppSpecific("Import: Layer");
        local4 = new com.adobe.ttpixel.ui::TTImage.TTImage();
        new com.adobe.ttpixel.ui::TTImage.TTImage().type = TTImage.TYPE_ORIGINAL;
        local4.initFromURL(local2, null, new com.adobe.cab.core::CABStateMessage.CABStateMessage("addLayerFromExternalImage", local0));
        local4.load();
    }
    "return";
}
