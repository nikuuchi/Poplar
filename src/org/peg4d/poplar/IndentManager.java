package org.peg4d.poplar;

public class IndentManager {
	private int level = 0;
	private String str = "    ";

	public IndentManager() { }

	public void indent() {
		++level;
	}

	public void dedent() {
		--level;
	}

	public String getIndentString() {
		StringBuilder s = new StringBuilder();
		for(int i = 0; i < level; i++) {
			s.append(str);
		}
		return s.toString();
	}
}