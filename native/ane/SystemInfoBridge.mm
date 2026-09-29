#include "SystemInfoBridge.hpp"

#import <UIKit/UIKit.h>

double SystemInfoScreenDPISquare()
{
    const double scale =
        static_cast<double>([UIScreen mainScreen].scale);

    UIDevice* device = [UIDevice currentDevice];

    double baseDPI = 163.0;

    if ([device respondsToSelector:@selector(userInterfaceIdiom)]) {
        const UIUserInterfaceIdiom idiom =
            device.userInterfaceIdiom;

        if (idiom == UIUserInterfaceIdiomPad) {
            /*
             * Original TTPixel uses a 132-DPI base for iPad, with
             * a special 163-DPI case for iPad2,5+.
             *
             * That exception applies only to 32-bit iPad2,x
             * hardware and is unreachable in this ARM64 rebuild.
             */
            baseDPI = 132.0;
        }
        else if (idiom != UIUserInterfaceIdiomPhone) {
            baseDPI = 160.0;
        }
    }

    return scale * baseDPI;
}
