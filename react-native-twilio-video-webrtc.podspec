require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name           = 'react-native-twilio-video-webrtc'
  s.version        = package['version']
  s.summary        = package['description']
  s.description    = package['description']
  s.license        = package['license']
  s.author         = package['author']
  s.homepage       = package['homepage']
  s.source         = { git: 'https://github.com/blackuy/react-native-twilio-video-web-rtc', tag: s.version }

  s.requires_arc   = true
  s.platform       = :ios, '8.0'

  s.preserve_paths = 'LICENSE', 'README.md', 'package.json', 'index.js'
  s.source_files   = 'ios/*.{h,m}'

  s.dependency 'React'
  s.dependency 'TwilioVideo', '~> 2.10.1'
  # 2.10.2 should fix iOS 13 simulator crashes
  # should upgrade once it becomes available
  # s.dependency 'TwilioVideo', '~> 2.10.2'
end
