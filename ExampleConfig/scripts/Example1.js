function serverReadyHandler(parser, date) {
	bot.log("Server Ready");
	parser.joinChannel("#AnotherChannel");
}

bot.bindEvent("onServerReady", "serverReadyHandler");
