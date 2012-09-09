// This script is an eample.

function serverReadyHandler(parser, date) {
	bot.log("Server Ready");
	parser.joinChannel("#AnotherChannel");
}

bot.bindEvent("onServerReady", "serverReadyHandler");

function onChannelMessage(parser, date, channel, client, message, host) {
	if (message.indexOf("!rehash") == 0) {
		channel.sendMessage("Reloading...");
		bot.rehash();
	} else if (message.indexOf("@") == 0) {
		command = message.indexOf(" ") > 0 ? message.substring(1, message.indexOf(" ")) : message.substring(1);
		args = message.indexOf(" ") > 0 ? message.substring(message.indexOf(" ") + 1) : ""
		
		if (command != "") {
			processCommand(parser, channel, client, command, args);
		}
	} else {
		channel.sendMessage(client + " said: " + message);
	}
}

function processCommand(parser, channel, client, command, args) {
	if (command == "raw") {
		channel.sendMessage("Sent raw: " + args);
		parser.sendRawMessage(args);
	} else if (command == "eval") {
		try {
			channel.sendMessage("Result: " + eval(args));
		} catch (e) {
			channel.sendMessage("Error: " + e);
		}
	}
}

bot.bindEvent("onChannelMessage", "onChannelMessage");
