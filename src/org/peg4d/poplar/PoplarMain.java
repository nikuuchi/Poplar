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
		options.addOption("d", false, "dump tree");

		CommandLineParser parser = new PosixParser();
		String inputFileName = "";
		String outputFileName = "/dev/stdout";
		boolean verbose = false;
		try {
			CommandLine cmd = parser.parse(options, args);
			if(cmd.hasOption("h")) {
				PoplarMain.showHelp(options);
				System.exit(0);
			}
			if(cmd.hasOption("f")) {
				inputFileName = cmd.getOptionValue("f");
			}
			if(cmd.hasOption("o")) {
				outputFileName = cmd.getOptionValue("o");
			}
			if(cmd.hasOption("d")) {
				verbose = true;
			}
		} catch (ParseException e) {
			PoplarMain.showHelp(options);
			System.exit(0);
		}

		Grammar peg = new GrammarFactory().newGrammar("main", "c99.p4d");
		ParsingSource ps = ParsingSource.loadSource(inputFileName);
		ParsingContext p = new ParsingContext(ps);
		ParsingObject o = p.parse(peg, "File");

		if(o != null) {
			CSourceGenerator generator = new CSourceGenerator(outputFileName);
			generator.writeC(o, verbose);
		} else {
			if(p.isFailure()) {
				Grammar fixer = new GrammarFactory().newGrammar("main", "c99_poplar.p4d");
				ParsingSource inputs = ParsingSource.loadSource(inputFileName);
				ParsingContext pc = new ParsingContext(inputs);
				ParsingObject po = pc.parse(fixer, "File");
				if(po != null) {
					PoplarCSourceGenerator generator = new PoplarCSourceGenerator(outputFileName);
					generator.writeC(po, verbose);
				} else {
					if(pc.isFailure()) {
						System.out.println(pc.source.formatPositionLine("error", pc.fpos, pc.getErrorMessage()));
						System.out.println(pc.source.formatPositionLine("maximum matched", pc.head_pos, ""));
						return;
					}
				}
			}
		}
	}

}
