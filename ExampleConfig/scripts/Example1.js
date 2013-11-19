//-------------------------------------------------------------------------
// Binds
//-------------------------------------------------------------------------
bot.bindEvent("onServerReady", "onServerReady");
irc.bindCommand("!rehash", "-", "doRehash");
irc.bindCommand("@eval", "-", "doEval");
irc.bindCommand("@raw", "-", "doRaw");


//-------------------------------------------------------------------------
// Bound Methods
//-------------------------------------------------------------------------
function onServerReady(parser, date) {
	bot.log("Server Ready 1");
}

function doRehash(parser, date, channel, client, command, args) {
	if (!irc.hasBotFlag(client, 'n')) { return; }

	channel.sendMessage("Reloading...");
	bot.rehash();
}

function doRaw(parser, date, channel, client, command, args) {
	if (!irc.hasBotFlag(client, 'n')) { return; }

	channel.sendMessage("Sent raw: " + args);
	parser.sendRawMessage(args);
}

function doEval(parser, date, channel, client, command, args) {
	if (!irc.hasBotFlag(client, 'n')) { return; }

	try {
		channel.sendMessage("Result: " + eval(args));
	} catch (e) {
		channel.sendMessage("Error: " + e);
	}
}
