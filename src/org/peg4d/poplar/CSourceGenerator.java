package org.peg4d.poplar;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.peg4d.ParsingObject;
import org.peg4d.ext.Generator;

public class CSourceGenerator extends Generator implements CTags {

	private IndentManager indent;

	public CSourceGenerator(String outputFileName) {
		super(outputFileName);
		indent = new IndentManager();
	}

	public void writeC(ParsingObject pego, boolean verbose) {
		if(verbose) {
			System.out.println(pego);
		}
		this.dispatch(pego);
		this.close();
	}

	protected void dispatch(ParsingObject pego) {
		Method m = null;
		String name = "gen" + pego.getTag().toString();
		try {
			m = this.getClass().getDeclaredMethod(name, ParsingObject.class);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		if(m != null) {
			try {
				m.invoke(this, pego);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void genSource(ParsingObject pego) {
		for(int i = 0; i < pego.size(); i++) {
			ParsingObject p = pego.get(i);
			if(!Is(p, "ExpressionStatement")) {
				this.dispatch(p);
			}
		}
	}

	protected void createAnnotation(ParsingObject annotations) {
		if(annotations.size() == 0) {
		} else {
			for(int i = 0; i < annotations.size(); i++) {
				ParsingObject annotation = annotations.get(i);
				this.dispatch(annotation);
				this.write(" ");
			}
		}
	}

	protected void createParameters(ParsingObject params) {
		this.write("(");
		int size = params.size() - 1;
		for(int i = 0; i < size; i++) {
			ParsingObject param = params.get(i);
			this.dispatch(param);
			this.write(", ");
		}
		if(size >= 0) {
			this.dispatch(params.get(size));
		}
		this.write(")");
	}

	@Override
	public void genFunction(ParsingObject pego) {
		this.createAnnotation(pego.get(0)); //Anno
		this.dispatch(pego.get(1)); //Return type
		this.write(" ");
		this.dispatch(pego.get(2)); //Name

		this.createParameters(pego.get(3)); //Params
		if(pego.size() == 4) {
			this.write(";\n");
			return;
		}

		if(pego.size() > 4) {
			this.dispatchWithoutEmpty(pego.get(4)); //Block or ; or Empty
			if(!Is(pego.get(4), "Block")) {
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
		if(!Is(pego.get(0), "TFunc")) {
			this.write(" ");
			this.dispatch(pego.get(1));
		}
		this.write(";\n");
	}

	@Override
	public void genVarDecl(ParsingObject pego) {
		this.dispatch(pego.get(0));
		if(pego.size() > 1) {
			this.write(" = ");
			this.dispatch(pego.get(1));
		}
	}

	@Override
	public void genDesignation(ParsingObject pego) {
		System.err.println("Designation");
	}

	@Override
	public void genTInt(ParsingObject pego) {
		this.write(pego.getText());
	}

	private void genTStructOrUnion(ParsingObject pego) {
		this.dispatch(pego.get(0));
		if(pego.size() == 1) {
			return;
		}
		this.write(" {\n");
		indent.indent();
		for(int i = 1; i < pego.size(); i++) {
			this.write(indent.getIndentString());
			this.dispatch(pego.get(i));
			this.write("\n");
		}
		indent.dedent();
		this.write("}");
	}

	@Override
	public void genTStruct(ParsingObject pego) {
		this.write("struct ");
		this.genTStructOrUnion(pego);
	}

	@Override
	public void genTUnion(ParsingObject pego) {
		this.write("union ");
		this.genTStructOrUnion(pego);
	}

	@Override
	public void genTConst(ParsingObject pego) {
		this.write("const ");
		this.dispatch(pego.get(0));
		if(pego.size() > 1) {
			this.dispatch(pego.get(1));
		}
	}

	@Override
	public void genTFunc(ParsingObject pego) {
		this.dispatch(pego.get(0)); //return Type
		this.write(" (* ");
		this.dispatch(pego.get(1)); //Name
		this.write(")");
		this.createParameters(pego.get(2)); //Params
	}

	@Override
	public void genTVoid(ParsingObject pego) {
		this.write("void");
	}

	@Override
	public void genTPointer(ParsingObject pego) {
		if(pego.size() == 0) {
			this.write("* ");
			return;
		}
		this.dispatch(pego.get(0));
		this.write("*");
	}

	@Override
	public void genTEnum(ParsingObject pego) {
		System.err.println("TEnum");
	}

	@Override
	public void genTBoolean(ParsingObject pego) {
		System.err.println("TBoolean");
	}

	@Override
	public void genTComplex(ParsingObject pego) {
		System.err.println("TComplex");
	}

	@Override
	public void genTName(ParsingObject pego) {
		this.write(pego.getText());
	}

	@Override
	public void genBit(ParsingObject pego) {
		System.err.println("Bit");
	}

	@Override
	public void genList(ParsingObject pego) {
		System.out.println("No longer need it");
	}

	@Override
	public void genBlock(ParsingObject pego) {
		this.write("{");
		if(pego.size() > 0) {
			if(!isMessage(pego.get(0))) {
				this.write("\n");
			}
		}
		indent.indent();
		for(int i = 0; i < pego.size(); i++) {
			ParsingObject p = pego.get(i);
			if(!isEmpty(p)) {
				this.write(indent.getIndentString());
			}
			this.dispatch(p);
			if(!(isEmpty(p) || isExpressionStatement(p))) {
				this.write("\n");
			}
		}
		indent.dedent();
		this.write(indent.getIndentString());
		this.write("}");
	}

	private boolean isExpressionStatement(ParsingObject p) {
		return p.getTag().toString().equals("ExpressionStatement");
	}

	protected boolean isMessage(ParsingObject pego) {
		return pego.getTag().toString().equals("Message");
	}

	@Override
	public void genIf(ParsingObject pego) {
		this.write("if(");
		this.dispatchWithoutEmpty(pego.get(0)); //(
		this.dispatchWithoutEmpty(pego.get(1)); //Cond
		this.dispatchWithoutEmpty(pego.get(2)); //)
		this.write(")");
		if(!Is(pego.get(3), "Block")) {
			this.write("\n");
			indent.indent();
			this.write(indent.getIndentString());
		}

		this.dispatchWithoutEmpty(pego.get(3)); //Block

		if(!Is(pego.get(3), "Block")) {
			this.indent.dedent();
		}
		if(pego.size() == 4) {
			return;
		}
		if(!Is(pego.get(3), "Block")) {
			this.write("\n");
			this.write(indent.getIndentString());
		}

		this.write("else ");

		if(!Is(pego.get(3), "Block")) {
			this.write("\n");
			indent.indent();
			this.write(indent.getIndentString());
		}
		this.dispatch(pego.get(4));
		if(!Is(pego.get(3), "Block")) {
			this.indent.dedent();
		}
	}

	protected boolean Is(ParsingObject parsingObject, String string) {
		return parsingObject.getTag().toString().equals(string);
	}

	//0: (, 1: cond, 2:), 3: {,  4: Block, 5: }
	@Override
	public void genWhile(ParsingObject pego) {
		this.write("while(");
		this.dispatchWithoutEmpty(pego.get(0)); //(
		this.dispatchWithoutEmpty(pego.get(1)); //Cond
		this.dispatchWithoutEmpty(pego.get(2)); //)
		this.write(")");

		this.dispatchWithoutEmpty(pego.get(3)); //Block

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
		this.write(");\n");
	}

	@Override
	public void genFor(ParsingObject pego) {
		this.write("for(");
		this.dispatchWithoutEmpty(pego.get(0)); // (
		this.dispatchWithoutEmpty(pego.get(1)); // Cond1
		if(!Is(pego.get(1), "Declaration")) {
			this.write(";");
		}
		this.dispatchWithoutEmpty(pego.get(2));
		this.dispatchWithoutEmpty(pego.get(3));
		this.write(";");
		this.dispatchWithoutEmpty(pego.get(4));
		if(!Is(pego.get(5), "Block")) {
			this.dispatchWithoutEmpty(pego.get(5));
			this.write(")");
			this.dispatchWithoutEmpty(pego.get(6));
		} else {
			this.write(")");
			this.dispatchWithoutEmpty(pego.get(5));
		}
		if(pego.size() > 7) {
			this.dispatchWithoutEmpty(pego.get(7));
		}
	}

	protected void dispatchWithoutEmpty(ParsingObject pego) {
		if(!isEmpty(pego)) {
			this.dispatch(pego);
		}
	}

	private boolean isEmpty(ParsingObject pego) {
		String tag = pego.getTag().toString();
		return tag.equals("Lbr") || tag.equals("Rbr") || tag.equals("#equal");
	}

	@Override
	public void genGoto(ParsingObject pego) {
		this.write("goto ");
		this.dispatch(pego.get(0));
		this.write(";\n");
	}

	@Override
	public void genContinue(ParsingObject pego) {
		this.write("continue;");
		if(pego.size() > 0) {
			this.dispatch(pego.get(0));
		}
	}

	@Override
	public void genBreak(ParsingObject pego) {
		this.write("break;");
		if(pego.size() > 0) {
			this.dispatch(pego.get(0));
		}
	}

	@Override
	public void genReturn(ParsingObject pego) {
		this.write("return");
		if(pego.size() == 0) {
			this.write(";");
			return;
		}
		ParsingObject exprOrMessage = pego.get(0);
		if(exprOrMessage.getTag().toString().equals("Message")) {
			this.write(";");
			this.dispatch(exprOrMessage);
		} else {
			this.write(" ");
			this.dispatch(exprOrMessage);
			this.write(";");
			if(pego.size() > 1) {
				this.dispatch(pego.get(1));
			}
		}
	}

	@Override
	public void genSwitch(ParsingObject pego) {
		this.write("switch(");
		this.dispatchWithoutEmpty(pego.get(0)); // (
		this.dispatchWithoutEmpty(pego.get(1)); // Cond
		this.dispatchWithoutEmpty(pego.get(2)); // )
		this.write(")");
		this.dispatchWithoutEmpty(pego.get(3)); // Block
		this.write("\n");
	}

	@Override
	public void genSwitchCase(ParsingObject pego) {
		this.write("case ");
		this.dispatchWithoutEmpty(pego.get(0)); // case
		this.write(":\n");
		indent.indent();
		for(int i = 1; i < pego.size(); i++) {
			this.write(indent.getIndentString());
			this.dispatchWithoutEmpty(pego.get(i));
			if(i < pego.size() - 1) {
				this.write("\n");
			}
		}
		indent.dedent();
		
	}

	@Override
	public void genSwitchDefault(ParsingObject pego) {
		this.write("default:\n");
		indent.indent();
		for(int i = 0; i < pego.size(); i++) {
			this.write(indent.getIndentString());
			this.dispatchWithoutEmpty(pego.get(i));
			if(i < pego.size() - 1) {
				this.write("\n");
			}
		}
		indent.dedent();
		
	}

	@Override
	public void genLabel(ParsingObject pego) {
		this.dispatch(pego.get(0));
		this.write(":\n");
	}

	@Override
	public void genEmpty(ParsingObject pego) {
		// Do nothing
	}

	@Override
	public void genAssign(ParsingObject pego) {
		this.createBinaryExpression(pego, " = ");
	}

	@Override
	public void genAssignMul(ParsingObject pego) {
		this.createBinaryExpression(pego, " *= ");
	}

	@Override
	public void genAssignDiv(ParsingObject pego) {
		this.createBinaryExpression(pego, " /= ");
	}

	@Override
	public void genAssignMod(ParsingObject pego) {
		this.createBinaryExpression(pego, " %= ");
	}

	@Override
	public void genAssignAdd(ParsingObject pego) {
		this.createBinaryExpression(pego, " += ");
	}

	@Override
	public void genAssignSub(ParsingObject pego) {
		this.createBinaryExpression(pego, " -= ");
		
	}

	@Override
	public void genAssignLeftShift(ParsingObject pego) {
		this.createBinaryExpression(pego, " <<= ");
	}

	@Override
	public void genAssignRightShift(ParsingObject pego) {
		this.createBinaryExpression(pego, " >>= ");
	}

	@Override
	public void genAssignBitwiseAnd(ParsingObject pego) {
		this.createBinaryExpression(pego, " &= ");
	}

	@Override
	public void genAssignBitwiseOr(ParsingObject pego) {
		this.createBinaryExpression(pego, " |= ");
	}

	@Override
	public void genAssignBitwiseXOr(ParsingObject pego) {
		this.createBinaryExpression(pego, " ^= ");
	}

	@Override
	public void genPrefixInc(ParsingObject pego) {
		this.write("++");
		this.dispatch(pego.get(0));
		
	}

	@Override
	public void genPrefixDec(ParsingObject pego) {
		this.write("--");
		this.dispatch(pego.get(0));
	}

	@Override
	public void genAddress(ParsingObject pego) {
		this.write("&");
		this.dispatch(pego.get(0));
	}

	@Override
	public void genStar(ParsingObject pego) {
		this.write("*");
		this.dispatch(pego.get(0));
	}

	@Override
	public void genPlus(ParsingObject pego) {
		this.write("+");
		this.dispatch(pego.get(0));
	}

	@Override
	public void genMinus(ParsingObject pego) {
		this.write("-");
		this.dispatch(pego.get(0));
	}

	@Override
	public void genCompl(ParsingObject pego) {
		this.write("~");
		this.dispatch(pego.get(0));
	}

	@Override
	public void genNot(ParsingObject pego) {
		this.write("!");
		this.dispatch(pego.get(0));
	}

	@Override
	public void genSizeOf(ParsingObject pego) {
		this.write("sizeof(");
		this.dispatch(pego.get(0));
		this.write(")");
	}

	@Override
	public void genCast(ParsingObject pego) {
		this.write("(");
		this.dispatch(pego.get(0));
		this.write(")");
		this.dispatch(pego.get(1));
	}

	@Override
	public void genField(ParsingObject pego) {
		this.dispatch(pego.get(0));
		this.write(".");
		this.dispatch(pego.get(1));
	}

	@Override
	public void genInc(ParsingObject pego) {
		this.dispatch(pego.get(0));
		this.write("++");
	}

	@Override
	public void genDec(ParsingObject pego) {
		this.dispatch(pego.get(0));
		this.write("--");
	}

	@Override
	public void genIndex(ParsingObject pego) {
		this.dispatch(pego.get(0));
		this.write("[");
		this.dispatch(pego.get(1));
		this.write("]");
	}

	@Override
	public void genApply(ParsingObject pego) {
		this.dispatch(pego.get(0));
		ParsingObject params = pego.get(1);
		this.write("(");
		if(params.size() > 0) {
			this.dispatch(params.get(0));
		}
		for(int i = 1; i < params.size(); i++) {
			ParsingObject param = params.get(i);
			if(!isMessage(param)) {
				this.write(", ");
			}
			this.dispatch(params.get(i));
		}
		this.write(")");
	}

	@Override
	public void genDeclaration(ParsingObject pego) {
		this.createAnnotation(pego.get(0));
		this.dispatch(pego.get(1));
		this.write(" ");
		this.dispatch(pego.get(2));
		if(pego.size() <= 3) {
			this.write(";");
		} else {
			for(int i = 3; i < pego.size(); i++) {
				if(isMessage(pego.get(i))) {
					this.dispatch(pego.get(i));
				} else {
					if(!Is(pego.get(i), "Rbr")) {
						this.write(", ");
						this.dispatch(pego.get(i));
					}
				}
			}
			this.write(";");
		}
	}

	@Override
	public void genName(ParsingObject pego) {
		this.write(pego.getText());
	}

	@Override
	public void genInteger(ParsingObject pego) {
		this.write(pego.getText());
	}

	private void createBinaryExpression(ParsingObject pego, String op) {
		this.dispatch(pego.get(0));
		this.write(op);
		this.dispatch(pego.get(1));
	}

	@Override
	public void genLessThan(ParsingObject pego) {
		this.createBinaryExpression(pego, " < ");
	}

	@Override
	public void genLessThanEquals(ParsingObject pego) {
		this.createBinaryExpression(pego, " <= ");
	}

	@Override
	public void genMessage(ParsingObject pego) {
		this.write(" /*" + pego.getText() + "*/");
	}

	@Override
	public void genMul(ParsingObject pego) {
		this.createBinaryExpression(pego, " * ");
	}

	@Override
	public void genDiv(ParsingObject pego) {
		this.createBinaryExpression(pego, " / ");
	}

	@Override
	public void genMod(ParsingObject pego) {
		this.createBinaryExpression(pego, " % ");
	}

	@Override
	public void genAdd(ParsingObject pego) {
		this.createBinaryExpression(pego, " + ");
	}

	@Override
	public void genSub(ParsingObject pego) {
		this.createBinaryExpression(pego, " - ");
	}

	@Override
	public void genExpressionStatement(ParsingObject pego) {
		this.dispatch(pego.get(0));
		this.write(";");
		if(pego.size() > 1) {
			this.dispatch(pego.get(1));
		}
		this.write("\n");
	}

	@Override
	public void genStructMemberDeclaration(ParsingObject pego) {
		this.createAnnotation(pego.get(0));
		this.dispatch(pego.get(1));
		this.write(" ");
		this.dispatch(pego.get(2));
		for(int i = 3; i < pego.size(); i++) {
			this.write(", ");
			this.dispatch(pego.get(i));
		}
		this.write(";");
	}

	@Override
	public void genParam(ParsingObject pego) {
		this.dispatch(pego.get(0));
		if(pego.size() > 1) {
			this.write(" ");
			this.dispatch(pego.get(1));
		}
	}

	@Override
	public void genPointerName(ParsingObject pego) {
		this.write("*");
		this.dispatch(pego.get(0));
	}

	@Override
	public void genArrayName(ParsingObject pego) {
		this.dispatch(pego.get(0));
		this.write("[");
		if(pego.size() > 1) {
			this.dispatch(pego.get(1));
		}
		this.write("]");
	}

	@Override
	public void genInitializer(ParsingObject pego) {
		this.write("{");
		if(pego.size() > 0) {
			this.dispatch(pego.get(0));
			for(int i = 1; i < pego.size(); i++) {
				this.write(", ");
				this.dispatch(pego.get(i));
			}
		}
		this.write("}");
	}

	@Override
	public void genStructDeclaration(ParsingObject pego) {
		this.dispatch(pego.get(0));
		this.write(";\n");
	}

	@Override
	public void genString(ParsingObject pego) {
		this.write("\"");
		this.write(pego.getText());
		this.write("\"");
	}

	@Override
	public void genEquals(ParsingObject pego) {
		this.createBinaryExpression(pego, " == ");
	}

	@Override
	public void genExpression(ParsingObject pego) {
		this.createBinaryExpression(pego, " , ");
	}

	@Override
	public void genInclude(ParsingObject pego) {
		this.write("#include");
		this.write(pego.getText());
		this.write("\n");
	}

	@Override
	public void genDefine(ParsingObject pego) {
		this.write("#define ");
		this.write(pego.getText());
		this.write("\n");
	}

	@Override
	public void genGreaterThanEquals(ParsingObject pego) {
		this.createBinaryExpression(pego, " >= ");
	}

	@Override
	public void genGreaterThan(ParsingObject pego) {
		this.createBinaryExpression(pego, " > ");
	}

	@Override
	public void genDeclarationStatement(ParsingObject pego) {
		this.dispatch(pego.get(0));
		this.write("\n");
	}

	@Override
	public void genOr(ParsingObject pego) {
		this.createBinaryExpression(pego, " || ");
	}

	@Override
	public void genAnd(ParsingObject pego) {
		this.createBinaryExpression(pego, " && ");
	}

	@Override
	public void genPointerField(ParsingObject pego) {
		this.dispatch(pego.get(0));
		this.write("->");
		this.dispatch(pego.get(1));
	}

	@Override
	public void genNotEquals(ParsingObject pego) {
		this.createBinaryExpression(pego, " != ");
	}

	@Override
	public void genKeyValue(ParsingObject pego) {
		this.write(pego.get(0).getText());
	}

	@Override
	public void genCharacter(ParsingObject pego) {
		this.write("'");
		this.write(pego.getText());
		this.write("'");
	}

	@Override
	public void genTFloat(ParsingObject pego) {
		this.write(pego.getText());
	}

	@Override
	public void genBitwiseAnd(ParsingObject pego) {
		this.createBinaryExpression(pego, " & ");
	}

	@Override
	public void genBitwiseOr(ParsingObject pego) {
		this.createBinaryExpression(pego, " | ");
	}

	@Override
	public void genBitwiseXor(ParsingObject pego) {
		this.createBinaryExpression(pego, " ^ ");
	}

	@Override
	public void genFloat(ParsingObject pego) {
		this.write(pego.getText());
	}

	@Override
	public void genLeftShift(ParsingObject pego) {
		this.createBinaryExpression(pego, " << ");
	}

	@Override
	public void genRightShift(ParsingObject pego) {
		this.createBinaryExpression(pego, " >> ");
	}

	@Override
	public void genTrinary(ParsingObject pego) {
		this.dispatch(pego.get(0));
		this.write(" ? ");
		this.dispatch(pego.get(1));
		this.write(" : ");
		this.dispatch(pego.get(2));
	}

	@Override
	public void genLbr(ParsingObject pego) {
	}

	@Override
	public void genRbr(ParsingObject pego) {
	}

}
