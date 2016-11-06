project 'ios/TWReactNativeTwilioVideoWebrtc.xcodeproj'
platform :ios, '9.0'

source 'https://github.com/CocoaPods/Specs.git'
source 'https://github.com/twilio/cocoapod-specs'

# Uncomment the next line to define a global platform for your project
# platform :ios, '9.0'

target 'TWReactNativeTwilioVideoWebrtc' do
  # Uncomment the next line if you're using Swift or would like to use dynamic frameworks
  use_frameworks!

  pod 'PureLayout'
  pod 'TwilioVideo', '1.0.0-beta1'

  # Pods for TWReactNativeTwilioVideoWebrtc

end

post_install do |installer|
  installer.pods_project.targets.each do |target|
    target.build_configurations.each do |config|
      config.build_settings['ENABLE_BITCODE'] = 'NO'
    end
  end
end
