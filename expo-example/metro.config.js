// Learn more https://docs.expo.io/guides/customizing-metro
const { getDefaultConfig: getExpoConfig } = require('expo/metro-config');
const { mergeConfig } = require('metro-config');
const { makeMetroConfig } = require('@rnx-kit/metro-config');
const MetroSymlinksResolver = require('@rnx-kit/metro-resolver-symlinks');
const path = require('path');

const config = mergeConfig(
  getExpoConfig(__dirname),
  makeMetroConfig({
    watchFolders: [path.resolve(__dirname, '../')],
    resolver: {
      resolveRequest: MetroSymlinksResolver(),
    },
  }),
);

module.exports = config;
