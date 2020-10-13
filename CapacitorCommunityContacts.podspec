
  Pod::Spec.new do |s|
    s.name = 'CapacitorCommunityContacts'
    s.version = '0.0.1'
    s.summary = 'Contacts Plugin for Capacitor'
    s.license = 'MIT'
    s.homepage = 'https://github.com/NicolaasZA/contacts'
    s.author = 'Jonathan Gerber'
    s.source = { :git => 'https://github.com/NicolaasZA/contacts', :tag => s.version.to_s }
    s.source_files = 'ios/Plugin/**/*.{swift,h,m,c,cc,mm,cpp}'
    s.ios.deployment_target  = '11.0'
    s.dependency 'Capacitor'
  end