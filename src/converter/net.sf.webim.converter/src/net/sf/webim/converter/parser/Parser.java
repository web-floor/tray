// Parser.java

package net.sf.webim.converter.parser;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;

import net.sf.webim.converter.xml.XmlArgument;
import net.sf.webim.converter.xml.XmlNode;

public class Parser {
	
	public Parser() {
	}
	
	private static final boolean DEBUG_SYNTAX = false;
	
	private StringBuffer sb;
	
	int killEnds = -1;
	byte[] buff;
	int l;
	
	private String rawText(int start, int end) {
		if( killEnds == start ) {
			while( start < end && (buff[start] == '\t' || buff[start] == ' ') )
				start++;
	
			if( start < end && buff[start] == '\r' )
				start++;
	
			if( start < end && buff[start] == '\n' )
				start++;
		}
		try {
			return new String(buff, start, end-start, "utf-8");
		} catch(UnsupportedEncodingException ex) {
			return "";
		}
	}
	
	void error( String s ) {
		System.err.println(s);
	}
	
	public String parse(String s) {
		l = 0;
		sb = new StringBuffer();
		try {
			buff = s.getBytes("utf-8");
		} catch( UnsupportedEncodingException ex ) {
			return null;
		}
		if( parse() )
			return sb.toString();
	
		return null;
	}
	
	private void checkTag(XmlNode node, String endTag) {
		if( !node.getTagName().equals(endTag) )
			error("Tag " + node.getTagName() + " is closed with " + endTag);
	}

	public class lapg_place {
		public int line, offset;

		public lapg_place( int line, int offset ) {
			this.line = line;
			this.offset = offset;
		}
	};

	public class lapg_symbol {
		public Object sym;
		public int  lexem, state;
		public lapg_place pos;
		public lapg_place endpos;
	};

	private static final short[] lapg_char2no = new short[] {
		   0,   1,   1,   1,   1,   1,   1,   1,   1,   2,   3,   1,   1,   4,   1,   1,
		   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,
		   5,   6,   7,   1,   1,   8,   1,   9,   1,   1,   1,   1,   1,  10,   1,  11,
		  12,  13,  14,  15,  16,  17,  18,  19,  20,  21,  22,   1,  23,  24,  25,   1,
		  26,  27,  28,  29,  30,  31,  32,  33,  34,  35,  36,  37,  38,  39,  40,  41,
		  42,  43,  44,  45,  46,  47,  48,  49,  50,  51,  52,   1,   1,   1,   1,  53,
		   1,  54,  55,  56,  57,  58,  59,  60,  61,  62,  63,  64,  65,  66,  67,  68,
		  69,  70,  71,  72,  73,  74,  75,  76,  77,  78,  79,   1,   1,   1,   1,   1,
		   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,
		   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,
		   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,
		   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,
		   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,
		   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,
		   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,
		   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,
	};

	private static final short[][] lapg_lexem = new short[][] {
		{  -2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   3,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2, },
		{  -1,  -1,   4,   4,   4,   4,  -1,   5,  -1,   6,  -1,   7,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,   8,  -1,   9,  10,  -1,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11, },
		{  -3,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,  -3,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2, },
		{  -4,  -4,  -4,  -4,  -4,  -4,  12,  -4,  13,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4, },
		{ -14, -14,   4,   4,   4,   4, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, },
		{  -1,   5,   5,  -1,   5,   5,   5,  14,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5, },
		{  -1,   6,   6,  -1,   6,   6,   6,   6,   6,  15,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6, },
		{ -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, },
		{ -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, },
		{ -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, },
		{ -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, },
		{  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  11,  -8,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  -8,  -8,  -8,  -8,  -8,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11, },
		{  -1,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  16,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12,  12, },
		{  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  17,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  18,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1, },
		{  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9, },
		{  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9, },
		{  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6, },
		{  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  19,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1, },
		{  -1,  18,  18,  18,  18,  18,  18,  18,  20,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18, },
		{  -1,  19,  19,  19,  19,  19,  19,  19,  19,  19,  21,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19, },
		{  -1,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  22,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18,  18, },
		{  -1,  19,  19,  19,  19,  19,  19,  19,  19,  19,  23,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19, },
		{  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7, },
		{  -1,  19,  19,  19,  19,  19,  19,  19,  24,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19, },
		{  -1,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  25,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19,  19, },
		{  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5, },
	};

	private static final int[] lapg_action = new int[] {
		  -1,   9,  -1,   6,   8,   7,  -3,   2,  -1,   5, -17, -27,   1,  -1,  -1,   3,
		  -1, -35,  -1, -45,  18,  -1,   4,  11,  -1,  14,  -1,  17,  -1,  19,  15,  16,
		  -1,  -2,
	};

	private static final short[] lapg_lalr = new short[] {
		   1,  -1,   2,  -1,   3,  -1,   4,  -1,   5,  -1,   0,   0,  -1,  -2,  10,  -1,
		   6,  10,   8,  10,  11,  10,  -1,  -2,   6,  -1,   8,  13,  11,  13,  -1,  -2,
		   9,  -1,   6,  20,   8,  20,  11,  20,  -1,  -2,   6,  -1,   8,  12,  11,  12,
		  -1,  -2,
	};

	private static final short[] lapg_sym_goto = new short[] {
		   0,   1,   5,   9,  13,  17,  21,  27,  28,  31,  32,  33,  35,  35,  36,  38,
		  42,  46,  48,  52,  55,  56,  57,  59,
	};

	private static final short[] lapg_sym_from = new short[] {
		  32,   0,   6,   8,  14,   0,   6,   8,  14,   0,   6,   8,  14,   0,   6,   8,
		  14,   0,   6,   8,  14,   2,  11,  13,  16,  19,  21,  24,  18,  26,  28,  17,
		  10,  13,  18,   0,   0,   8,   0,   6,   8,  14,   0,   6,   8,  14,   8,  14,
		   0,   6,   8,  14,   2,  13,  21,  11,  11,  11,  19,
	};

	private static final short[] lapg_sym_to = new short[] {
		  33,   1,   1,   1,   1,   2,   2,  13,  13,   3,   3,   3,   3,   4,   4,   4,
		   4,   5,   5,   5,   5,  10,  17,  10,  23,  17,  10,  29,  25,  30,  31,  24,
		  16,  21,  26,  32,   6,  14,   7,  12,   7,  12,   8,   8,   8,   8,  15,  22,
		   9,   9,   9,   9,  11,  11,  28,  18,  19,  20,  27,
	};

	private static final short[] lapg_rlen = new short[] {
		   1,   2,   1,   2,   3,   1,   1,   1,   1,   1,   1,   3,   1,   0,   4,   5,
		   4,   2,   1,   3,   1,
	};

	private static final short[] lapg_rlex = new short[] {
		  13,  14,  14,  15,  15,  15,  15,  15,  15,  15,  19,  19,  20,  20,  16,  18,
		  17,  21,  21,  22,  22,
	};

	private static final String[] lapg_syms = new String[] {
		"eoi",
		"any",
		"'<'",
		"comment",
		"doctype",
		"taglib",
		"identifier",
		"ccon",
		"'>'",
		"'='",
		"':'",
		"'/'",
		"_skip",
		"input",
		"xml_tags",
		"xml_tag_or_space",
		"tag_start",
		"tag_end",
		"no_body_tag",
		"tag_name",
		"argumentsopt",
		"arguments",
		"argument",
	};

	public enum Tokens {
		eoi,
		any,
		LESS,
		comment,
		doctype,
		taglib,
		identifier,
		ccon,
		GREATER,
		EQ,
		COLON,
		DIV,
		_skip,
		input,
		xml_tags,
		xml_tag_or_space,
		tag_start,
		tag_end,
		no_body_tag,
		tag_name,
		argumentsopt,
		arguments,
		argument,
	}

	private static int lapg_next( int state, int symbol ) {
		int p;
		if( lapg_action[state] < -2 ) {
			for( p = - lapg_action[state] - 3; lapg_lalr[p] >= 0; p += 2 )
				if( lapg_lalr[p] == symbol ) break;
			return lapg_lalr[p+1];
		}
		return lapg_action[state];
	}

	private static int lapg_state_sym( int state, int symbol ) {
		int min = lapg_sym_goto[symbol], max = lapg_sym_goto[symbol+1]-1;
		int i, e;

		while( min <= max ) {
			e = (min + max) >> 1;
			i = lapg_sym_from[e];
			if( i == state )
				return lapg_sym_to[e];
			else if( i < state )
				min = e + 1;
			else
				max = e - 1;
		}
		return -1;
	}

	public boolean parse() {

		byte[]        token = new byte[1024];
		int           lapg_head = 0, group = 0, lapg_i, lapg_size, chr;
		lapg_symbol[] lapg_m = new lapg_symbol[1024];
		lapg_symbol   lapg_n;
		int           lapg_current_line = 1, lapg_current_offset = 0;

		lapg_m[0] = new lapg_symbol();
		lapg_m[0].state = 0;
		chr = l < buff.length ? buff[l++] : 0;

		do {
			lapg_n = new lapg_symbol();
			lapg_n.pos = new lapg_place( lapg_current_line, lapg_current_offset );
			for( lapg_size = 0, lapg_i = group; lapg_i >= 0; ) {
				if( lapg_size < 1024-1 ) token[lapg_size++] = (byte)chr;
				lapg_i = lapg_lexem[lapg_i][lapg_char2no[(chr+256)%256]];
				if( lapg_i >= -1 && chr != 0 ) { 
					lapg_current_offset++;
					if( chr == '\n' ) lapg_current_line++;
					chr = l < buff.length ? buff[l++] : 0;
				}
			}
			lapg_n.endpos = new lapg_place( lapg_current_line, lapg_current_offset );

			if( lapg_i == -1 ) {
				if( chr == 0 ) {
					error( "Unexpected end of file reached");
					break;
				}
				error( MessageFormat.format( "invalid lexem at line {0}: `{1}`, skipped", lapg_n.pos.line, new String(token,0,lapg_size) ) );
				lapg_n.lexem = -1;
				continue;
			}

			lapg_size--;
			lapg_n.lexem = -lapg_i-2;
			lapg_n.sym = null;

			switch( lapg_n.lexem ) {
				case 2:
					 group = 1; break; 
				case 6:
					 lapg_n.sym = new String(token,0,lapg_size); break; 
				case 7:
					 lapg_n.sym = new String(token,1,lapg_size-2); break; 
				case 8:
					 group = 0; break; 
				case 12:
					 continue;
			}


			do {
				lapg_i = lapg_next( lapg_m[lapg_head].state, lapg_n.lexem );

				if( lapg_i >= 0 ) {
					lapg_symbol lapg_gg = new lapg_symbol();
					lapg_gg.sym = (lapg_rlen[lapg_i]!=0)?lapg_m[lapg_head+1-lapg_rlen[lapg_i]].sym:null;
					lapg_gg.lexem = lapg_rlex[lapg_i];
					lapg_gg.state = 0;
					if( DEBUG_SYNTAX )
						System.out.println( "reduce to " + lapg_syms[lapg_rlex[lapg_i]] );
					lapg_gg.pos = (lapg_rlen[lapg_i]!=0)?lapg_m[lapg_head+1-lapg_rlen[lapg_i]].pos:lapg_n.pos;
					lapg_gg.endpos = (lapg_rlen[lapg_i]!=0)?lapg_m[lapg_head].endpos:lapg_n.pos;
					switch( lapg_i ) {
						case 3:
							 checkTag(((XmlNode)lapg_m[lapg_head-1].sym),((String)lapg_m[lapg_head-0].sym)); 
							break;
						case 4:
							 checkTag(((XmlNode)lapg_m[lapg_head-2].sym),((String)lapg_m[lapg_head-0].sym)); 
							break;
						case 10:
							 lapg_gg.sym = ((String)lapg_m[lapg_head-0].sym); 
							break;
						case 11:
							 lapg_gg.sym = ((String)lapg_m[lapg_head-2].sym) + ":" + ((String)lapg_m[lapg_head-0].sym); 
							break;
						case 14:
							 lapg_gg.sym = new XmlNode(((String)lapg_m[lapg_head-2].sym), ((ArrayList<XmlArgument>)lapg_m[lapg_head-1].sym)); 
							break;
						case 15:
							 lapg_gg.sym = new XmlNode(((String)lapg_m[lapg_head-3].sym), ((ArrayList<XmlArgument>)lapg_m[lapg_head-2].sym)); 
							break;
						case 16:
							 lapg_gg.sym = ((String)lapg_m[lapg_head-1].sym); 
							break;
						case 17:
							 ((ArrayList<XmlArgument>)lapg_gg.sym).add(((XmlArgument)lapg_m[lapg_head-0].sym)); 
							break;
						case 18:
							 lapg_gg.sym = new ArrayList<XmlArgument>(); ((ArrayList<XmlArgument>)lapg_gg.sym).add(((XmlArgument)lapg_m[lapg_head-0].sym)); 
							break;
						case 19:
							 lapg_gg.sym = new XmlArgument(); 
							break;
						case 20:
							 lapg_gg.sym = new XmlArgument(); 
							break;
					}
					for( int e = lapg_rlen[lapg_i]; e > 0; e-- ) 
						lapg_m[lapg_head--] = null;
					lapg_m[++lapg_head] = lapg_gg;
					lapg_m[lapg_head].state = lapg_state_sym( lapg_m[lapg_head-1].state, lapg_gg.lexem );
				} else if( lapg_i == -1 ) {
					lapg_m[++lapg_head] = lapg_n;
					lapg_m[lapg_head].state = lapg_state_sym( lapg_m[lapg_head-1].state, lapg_n.lexem );
					if( DEBUG_SYNTAX )
						System.out.println( MessageFormat.format( "shift: {0} ({1})", lapg_syms[lapg_n.lexem], new String(token,0,lapg_size) ) );
				}

			} while( lapg_i >= 0 && lapg_m[lapg_head].state != -1 );

			if( (lapg_i == -2 || lapg_m[lapg_head].state == -1) && lapg_n.lexem != 0 ) {
				break;
			}

		} while( lapg_n.lexem != 0 );

		if( lapg_m[lapg_head].state != 34-1 ) {
			error( MessageFormat.format( "syntax error before line {0}", lapg_n.pos.line ) );
			return false;
		};
		return true;
	}
}
