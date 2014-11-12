package org.peg4d.poplar;

import org.peg4d.ParsingObject;

public class PoplarCSourceGenerator extends CSourceGenerator {

	public PoplarCSourceGenerator(String outputFileName) {
		super(outputFileName);
	}

	@Override
	public void genFunction(ParsingObject pego) {
		this.createAnnotation(pego.get(0)); //Anno
		this.dispatch(pego.get(1)); //Return type
		this.write(" ");
		this.dispatch(pego.get(2)); //Name

		//FIXME For variable declaration?
		if(isMessage(pego.get(3)) && isMessage(pego.get(5)) && pego.get(4).size() == 0) {
			this.write(";\n");
			return;
		}

		this.dispatchWithoutEmpty(pego.get(3)); //(
		this.createParameters(pego.get(4)); //Params
		if(pego.size() == 5) {
			this.write(";\n");
			return;
		}
		this.dispatchWithoutEmpty(pego.get(5)); //)

		if(pego.size() > 6) {
			this.dispatchWithoutEmpty(pego.get(6)); //Block or ; or Empty
			if(!Is(pego.get(6), "Block")) {
				this.write(";");
			}
			this.write("\n");
		} else {
			this.write(";\n");
		}
	}

	@Override
	public void genTypeDeclaration(ParsingObject pego) {
		this.write("typedef ");
		this.dispatch(pego.get(0));
		if(Is(pego.get(0), "TFunc")) {
			this.write(";");
			if(isMessage(pego.get(1))) {
				this.dispatch(pego.get(1));
			}
			this.write("\n");
		} else {
			this.write(" ");
			this.dispatch(pego.get(1));
			this.write(";\n");
		}
	}

	@Override
	public void genDoWhile(ParsingObject pego) {
		this.write("do");
		this.dispatch(pego.get(0)); //Block
		this.write("while(");
		this.dispatchWithoutEmpty(pego.get(1)); // (
		this.dispatch(pego.get(2)); //Cond
		if(pego.size() > 3) {
			this.dispatchWithoutEmpty(pego.get(3)); // )
		}
		this.write(");");
		if(pego.size() == 5) {
			this.dispatch(pego.get(4)); //;
		}
		this.write("\n");
	}
}
