// Decompiled by Phoenix

function method1902()
{
    if (!!com.adobe.ttpixel.extension::TTNEUtils.getExtensionContext())
    {
        com.adobe.ttpixel.extension::TTNEUtils.getExtensionContext().addEventListener(StatusEvent.STATUS, local0.handleOSNotification);
    }
    "return";
}