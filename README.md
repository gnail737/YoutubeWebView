This Project is a basic Youtube WebView with automatical downloading ability when the video page is not DRM protected. It does not
have any dependencies on Android L WebView's new API, but the build dependencies file has target API Level 22, because lower Version WebView's
Security Vulnerabilities. You can modify it manually for lower API level(such as 4.4.2) and use it at your own risk.

The downloading will start automatically when you enters a youtube page matches url pattern "youtube.com/watch?v=", it is using the embeded
MP4 URL for 360P quality video. Default downloading directory is $SD_CARD_ROOT/YoutubeVideo, App will automatically create this Directory
YoutubeVideo under $SD_CARD_ROOT if nothing exists.
