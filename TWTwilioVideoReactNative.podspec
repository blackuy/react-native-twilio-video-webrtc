require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|

  s.name         = "TWTwilioVideoReactNative"
  s.version      = package["version"]
  s.summary      = "Twilio Video WebRTC for React Native."

  # s.description  = <<-DESC
  #                   Twilio Video WebRTC for React Native. 
  #                  DESC

  s.homepage     = "https://github.com/gaston23/react-native-twilio-video-webrtc"

  s.license      = "MIT"

  s.author       = { "Gaston Morixe" => "gaston@black.uy" }

  s.source       = { git: "https://github.com/gaston23/react-native-twilio-video-webrtc", tag: "#{s.version}" }

  s.source_files  = "Classes", "**/*.{h,m}"
  # s.exclude_files = "Classes/Exclude"

  # This is the key line. You must add it by hand.
  s.dependency 'TwilioVideo', '1.0.0-beta1'
  s.dependency 'PureLayout', '~> 3.0.2'

end

# platform :ios, '9.0'
# source 'https://github.com/CocoaPods/Specs.git'
# source 'https://github.com/twilio/cocoapod-specs'
# pod 'PureLayout'
# pod 'TwilioVideo', '1.0.0-beta1'