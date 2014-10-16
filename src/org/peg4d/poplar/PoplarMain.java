package org.peg4d.poplar;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.peg4d.Grammar;
import org.peg4d.GrammarFactory;
import org.peg4d.Main;
import org.peg4d.ParsingContext;
import org.peg4d.ParsingObject;
import org.peg4d.ParsingSource;

public class PoplarMain {

	public static void showHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("konoha4e", options );
	}

	public static void main(String[] args) {
		Options options = new Options();
		options.addOption("h", false, "show this help");
		options.addOption("f", true, "input file");
		options.addOption("o", true, "output file");

		CommandLineParser parser = new PosixParser();
		CSourceGenerator generator = new CSourceGenerator("/dev/stdout");
		String inputFileName = "";
		try {
			CommandLine cmd = parser.parse(options, args);
			if(cmd.hasOption("h")) {
				PoplarMain.showHelp(options);
				System.exit(0);
			}
			if(cmd.hasOption("f")) {
				inputFileName = cmd.getOptionValue("f");
			}
		} catch (ParseException e) {
			PoplarMain.showHelp(options);
			System.exit(0);
		}

		Grammar peg = new GrammarFactory().newGrammar("main", "c99.p4d");
		ParsingSource ps = Main.loadSource(peg, inputFileName);
		ParsingContext p = new ParsingContext(ps);
		ParsingObject o = p.parse(peg, "File");
		if(generator != null) {
			generator.writeC(o);
		}
	}

}
