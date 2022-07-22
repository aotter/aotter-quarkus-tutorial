const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  outputDir: '../resources/META-INF/resources/assets/webapp',
  indexPath: '../../../../templates/ConsoleResource/index.html',
  publicPath: '/assets/webapp'
})
