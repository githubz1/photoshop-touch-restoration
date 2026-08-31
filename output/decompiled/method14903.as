// Decompiled by Phoenix

function method14903()
{
    local4 = null;
    if (!(local2 == null))
    {
        local5 = local0.iCffDataDecoded[KEY_TOPDICTINDEX][local1][KEY_CHARSET];
        local4 = new Array();
        for (local6 in local2)
        {
            local4.push(local5[local6]);
        }
        local4 = local4.sort(Array.NUMERIC);
    }
    return local4;
}