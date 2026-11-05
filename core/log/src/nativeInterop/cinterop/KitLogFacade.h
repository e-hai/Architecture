#import <Foundation/Foundation.h>

@interface KitLogFacade : NSObject
+ (void)initializeWithDisk:(BOOL)withDisk;
+ (void)d:(NSString *)tag message:(NSString *)message;
+ (void)i:(NSString *)tag message:(NSString *)message;
+ (void)w:(NSString *)tag message:(NSString *)message;
+ (void)e:(NSString *)tag message:(NSString *)message errorDescription:(NSString * _Nullable)errorDescription;
@end
