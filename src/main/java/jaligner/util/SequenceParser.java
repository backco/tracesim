/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package jaligner.util;

import jaligner.Sequence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SequenceParser to sequences from different formats.
 * <br>
 * The supported formats are:
 * <ul>
 * <li>Plain sequence</li>, and
 * <li><a href="http://en.wikipedia.org/wiki/FASTA_format">FASTA</a></li>
 * </ul>
 *
 * @author Ahmed Moustafa
 */

public class SequenceParser {

    /**
     * Logger
     */
    private static final Logger logger = Logger.getLogger(SequenceParser.class.getName());

    /**
     * Returns a parsed Sequence from a string.
     *
     * @param sequence string to parse
     * @return parsed sequence
     * @throws SequenceParserException
     * @see Sequence
     */
    public static Sequence parse ( String sequence ) throws SequenceParserException {

	if ( sequence == null ) {
	    throw new SequenceParserException("Null sequence");
	}

	if ( sequence.trim().length() == 0 ) {
	    throw new SequenceParserException("Empty sequence");
	}

	sequence = sequence.replaceAll("\r\n", "\n");

	String sequenceName        = null;
	String sequenceDescription = null;

	if ( sequence.startsWith(">") ) {
	    // FASTA format
	    int index = sequence.indexOf("\n");

	    if ( index == -1 ) {
		throw new SequenceParserException("Invalid sequence format");
	    }

	    String first = sequence.substring(1, index);
	    sequence = sequence.substring(index);

	    index = 0;
	    for ( int i = 0; i < first.length() && first.charAt(i) != ' ' && first.charAt(i) != '\t'; i++, index++ ) {
		// Skip white spaces
	    }
	    sequenceName = first.substring(0, index);
	    StringTokenizer stringTokenizer = new StringTokenizer(sequenceName, "|");
	    while ( stringTokenizer.hasMoreTokens() ) {
		sequenceName = stringTokenizer.nextToken();
	    }
	    sequenceDescription = index + 1 > first.length() ? "" : first.substring(index + 1);
	} else {
	    // Plain format ... nothing to do here
	}

	Sequence s = new Sequence(prepare(sequence), sequenceName, sequenceDescription, Sequence.PROTEIN);

	return s;
    }

    /**
     * Returns a Sequence parsed and loaded from a file
     *
     * @param file to parse
     * @return parsed sequence
     * @throws SequenceParserException
     * @see Sequence
     */
    public static Sequence parse ( File file ) throws SequenceParserException {

	String         sequenceName        = null;
	String         sequenceDescription = null;
	BufferedReader reader              = null;
	try {
	    reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
	    StringBuffer buffer = new StringBuffer();

	    // Read & parse the first line
	    String line = reader.readLine();

	    if ( line.startsWith(">") ) {
		// FASTA sequence

		line = line.substring(1).trim();
		int index = 0;
		for ( int i = 0; i < line.length() && line.charAt(i) != ' ' && line.charAt(i) != '\t'; i++, index++ ) {
		    // Skip white spaces
		}

		sequenceName = line.substring(0, index);
		StringTokenizer stringTokenizer = new StringTokenizer(sequenceName, "|");
		while ( stringTokenizer.hasMoreTokens() ) {
		    sequenceName = stringTokenizer.nextToken();
		}
		sequenceDescription = index + 1 > line.length() ? "" : line.substring(index + 1);
	    } else {
		// Plain sequence
		buffer.append(prepare(line));
	    }

	    // Read the remaining the file (the actual sequence)
	    while ( ( line = reader.readLine() ) != null ) {
		buffer.append(prepare(line));
	    }
	    reader.close();

	    Sequence s = new Sequence(buffer.toString(), sequenceName, sequenceDescription, Sequence.PROTEIN);
	    return s;
	} catch ( Exception e ) {
	    throw new SequenceParserException(e.getMessage());
	} finally {
	    if ( reader != null ) {
		try {
		    reader.close();
		} catch ( Exception silent ) {
		    logger.log(Level.WARNING, "Failed closing reader: " + silent.getMessage(), silent);
		}
	    }
	}

    }

    /**
     * Removes whitespaces from a sequence and validates other characters.
     *
     * @param sequence sequence to be prepared
     * @return prepared array of characters
     * @throws SequenceParserException
     */
    private static String prepare ( String sequence ) throws SequenceParserException {

	StringBuffer buffer = new StringBuffer();
	//String copy = sequence.trim().toUpperCase();
	String copy = sequence;
	for ( int i = 0, n = copy.length(); i < n; i++ ) {
	    switch ( copy.charAt(i) ) {
		// skip whitespaces
		case 9:
		case 10:
		case 13:
		case 32:
		    break;

		// add a valid character
		case '0':
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9':
		case 'A':
		case 'B':
		case 'C':
		case 'D':
		case 'E':
		case 'F':
		case 'G':
		case 'H':
		case 'I':
		case 'J':
		case 'K':
		case 'L':
		case 'M':
		case 'N':
		case 'O':
		case 'P':
		case 'Q':
		case 'R':
		case 'S':
		case 'T':
		case 'U':
		case 'V':
		case 'W':
		case 'X':
		case 'Y':
		case 'Z':
		case 'a':
		case 'b':
		case 'c':
		case 'd':
		case 'e':
		case 'f':
		case 'g':
		case 'h':
		case 'i':
		case 'j':
		case 'k':
		case 'l':
		case 'm':
		case 'n':
		case 'o':
		case 'p':
		case 'q':
		case 'r':
		case 's':
		case 't':
		case 'u':
		case 'v':
		case 'w':
		case 'x':
		case 'y':
		case 'z':
		case 'À':
		case 'Á':
		case 'Â':
		case 'Ã':
		case 'Ä':
		case 'Å':
		case 'Æ':
		case 'Ç':
		case 'È':
		case 'É':
		case 'Ê':
		case 'Ë':
		case 'Ì':
		case 'Í':
		case 'Î':
		case 'Ï':
		case 'Ð':
		case 'Ñ':
		case 'Ò':
		case 'Ó':
		case 'Ô':
		case 'Õ':
		case 'Ö':
		case '×':
		case 'Ø':
		case 'Ù':
		case 'Ú':
		case 'Û':
		case 'Ü':
		case 'Ý':
		case 'Þ':
		case 'ß':
		case 'à':
		case 'á':
		case 'â':
		case 'ã':
		case 'ä':
		case 'å':
		case 'æ':
		case 'ç':
		case 'è':
		case 'é':
		case 'ê':
		case 'ë':
		case 'ì':
		case 'í':
		case 'î':
		case 'ï':
		case 'ð':
		case 'ñ':
		case 'ò':
		case 'ó':
		case 'ô':
		case 'õ':
		case 'ö':
		case '÷':
		case 'ø':
		case 'ù':
		case 'ú':
		case 'û':
		case 'ü':
		case 'ý':
		case 'þ':
		case 'ÿ':
		case 'Ā':
		case 'ā':
		case 'Ă':
		case 'ă':
		case 'Ą':
		case 'ą':
		case 'Ć':
		case 'ć':
		case 'Ĉ':
		case 'ĉ':
		case 'Ċ':
		case 'ċ':
		case 'Č':
		case 'č':
		case 'Ď':
		case 'ď':
		case 'Đ':
		case 'đ':
		case 'Ē':
		case 'ē':
		case 'Ĕ':
		case 'ĕ':
		case 'Ė':
		case 'ė':
		case 'Ę':
		case 'ę':
		case 'Ě':
		case 'ě':
		case 'Ĝ':
		case 'ĝ':
		case 'Ğ':
		case 'ğ':
		case 'Ġ':
		case 'ġ':
		case 'Ģ':
		case 'ģ':
		case 'Ĥ':
		case 'ĥ':
		case 'Ħ':
		case 'ħ':
		case 'Ĩ':
		case 'ĩ':
		case 'Ī':
		case 'ī':
		case 'Ĭ':
		case 'ĭ':
		case 'Į':
		case 'į':
		case 'İ':
		case 'ı':
		case 'Ĳ':
		case 'ĳ':
		case 'Ĵ':
		case 'ĵ':
		case 'Ķ':
		case 'ķ':
		case 'ĸ':
		case 'Ĺ':
		case 'ĺ':
		case 'Ļ':
		case 'ļ':
		case 'Ľ':
		case 'ľ':
		case 'Ŀ':
		case 'ŀ':
		case 'Ł':
		case 'ł':
		case 'Ń':
		case 'ń':
		case 'Ņ':
		case 'ņ':
		case 'Ň':
		case 'ň':
		case 'ŉ':
		case 'Ŋ':
		case 'ŋ':
		case 'Ō':
		case 'ō':
		case 'Ŏ':
		case 'ŏ':
		case 'Ő':
		case 'ő':
		case 'Œ':
		case 'œ':
		case 'Ŕ':
		case 'ŕ':
		case 'Ŗ':
		case 'ŗ':
		case 'Ř':
		case 'ř':
		case 'Ś':
		case 'ś':
		case 'Ŝ':
		case 'ŝ':
		case 'Ş':
		case 'ş':
		case 'Š':
		case 'š':
		case 'Ţ':
		case 'ţ':
		case 'Ť':
		case 'ť':
		case 'Ŧ':
		case 'ŧ':
		case 'Ũ':
		case 'ũ':
		case 'Ū':
		case 'ū':
		case 'Ŭ':
		case 'ŭ':
		case 'Ů':
		case 'ů':
		case 'Ű':
		case 'ű':
		case 'Ų':
		case 'ų':
		case 'Ŵ':
		case 'ŵ':
		case 'Ŷ':
		case 'ŷ':
		case 'Ÿ':
		case 'Ź':
		case 'ź':
		case 'Ż':
		case 'ż':
		case 'Ž':
		case 'ž':
		case 'ſ':
		case 'ƀ':
		case 'Ɓ':
		case 'Ƃ':
		case 'ƃ':
		case 'Ƅ':
		case 'ƅ':
		case 'Ɔ':
		case 'Ƈ':
		case 'ƈ':
		case 'Ɖ':
		case 'Ɗ':
		case 'Ƌ':
		case 'ƌ':
		case 'ƍ':
		case 'Ǝ':
		case 'Ə':
		case 'Ɛ':
		case 'Ƒ':
		case 'ƒ':
		case 'Ɠ':
		case 'Ɣ':
		case 'ƕ':
		case 'Ɩ':
		case 'Ɨ':
		case 'Ƙ':
		case 'ƙ':
		case 'ƚ':
		case 'ƛ':
		case 'Ɯ':
		case 'Ɲ':
		case 'ƞ':
		case 'Ɵ':
		case 'Ơ':
		case 'ơ':
		case 'Ƣ':
		case 'ƣ':
		case 'Ƥ':
		case 'ƥ':
		case 'Ʀ':
		case 'Ƨ':
		case 'ƨ':
		case 'Ʃ':
		case 'ƪ':
		case 'ƫ':
		case 'Ƭ':
		case 'ƭ':
		case 'Ʈ':
		case 'Ư':
		case 'ư':
		case 'Ʊ':
		case 'Ʋ':
		case 'Ƴ':
		case 'ƴ':
		case 'Ƶ':
		case 'ƶ':
		case 'Ʒ':
		case 'Ƹ':
		case 'ƹ':
		case 'ƺ':
		case 'ƻ':
		case 'Ƽ':
		case 'ƽ':
		case 'ƾ':
		case 'ƿ':
		case 'ǀ':
		case 'ǁ':
		case 'ǂ':
		case 'ǃ':
		case 'Α':
		case 'Β':
		case 'Γ':
		case 'Δ':
		case 'Ε':
		case 'Ζ':
		case 'Η':
		case 'Θ':
		case 'Ι':
		case 'Κ':
		case 'Λ':
		case 'Μ':
		case 'Ν':
		case 'Ξ':
		case 'Ο':
		case 'Π':
		case 'Ρ':
		case 'Σ':
		case 'Τ':
		case 'Υ':
		case 'Φ':
		case 'Χ':
		case 'Ψ':
		case 'Ω':
		case 'Ϊ':
		case 'Ϋ':
		case 'ά':
		case 'έ':
		case 'ή':
		case 'ί':
		case 'ΰ':
		case 'α':
		case 'β':
		case 'γ':
		case 'δ':
		case 'ε':
		case 'ζ':
		case 'η':
		case 'θ':
		case 'ι':
		case 'κ':
		case 'λ':
		case 'μ':
		case 'ν':
		case 'ξ':
		case 'ο':
		case 'π':
		case 'ρ':
		case 'ς':
		case 'σ':
		case 'τ':
		case 'υ':
		case 'φ':
		case 'χ':
		case 'ψ':
		case 'ω':
		case 'ϊ':
		case 'ϋ':
		case 'ό':
		case 'ύ':
		case 'ώ':
		case 'Ϗ':
		case 'ϐ':
		case 'ϑ':
		case 'ϒ':
		case 'ϓ':
		case 'ϔ':
		case 'ϕ':
		case 'ϖ':
		case 'ϗ':
		case 'Ϙ':
		case 'ϙ':
		case 'Ϛ':
		case 'ϛ':
		case 'Ϝ':
		case 'ϝ':
		case 'Ϟ':
		case 'ϟ':
		case 'Ϡ':
		case 'ϡ':
		case 'Ϣ':
		case 'ϣ':
		case 'Ϥ':
		case 'ϥ':
		case 'Ϧ':
		case 'ϧ':
		case 'Ϩ':
		case 'ϩ':
		case 'Ϫ':
		case 'ϫ':
		case 'Ϭ':
		case 'ϭ':
		case 'Ϯ':
		case 'ϯ':
		case 'ϰ':
		case 'ϱ':
		case 'ϲ':
		case 'ϳ':
		case 'ϴ':
		case 'ϵ':
		case '϶':
		case 'Ϸ':
		case 'ϸ':
		case 'Ϲ':
		case 'Ϻ':
		case 'ϻ':
		case 'ϼ':
		case 'Ͻ':
		case 'Ͼ':
		case 'Ͽ':
		case 'Ѐ':
		case 'Ё':
		case 'Ђ':
		case 'Ѓ':
		case 'Є':
		case 'Ѕ':
		case 'І':
		case 'Ї':
		case 'Ј':
		case 'Љ':
		case 'Њ':
		case 'Ћ':
		case 'Ќ':
		case 'Ѝ':
		case 'Ў':
		case 'Џ':
		case 'А':
		case 'Б':
		case 'В':
		case 'Г':
		case 'Д':
		case 'Е':
		case 'Ж':
		case 'З':
		case 'И':
		case 'Й':
		case 'К':
		case 'Л':
		case 'М':
		case 'Н':
		case 'О':
		case 'П':
		case 'Р':
		case 'С':
		case 'Т':
		case 'У':
		case 'Ф':
		case 'Х':
		case 'Ц':
		case 'Ч':
		case 'Ш':
		case 'Щ':
		case 'Ъ':
		case 'Ы':
		case 'Ь':
		case 'Э':
		case 'Ю':
		case 'Я':
		case 'а':
		case 'б':
		case 'в':
		case 'г':
		case 'д':
		case 'е':
		case 'ж':
		case 'з':
		case 'и':
		case 'й':
		case 'к':
		case 'л':
		case 'м':
		case 'н':
		case 'о':
		case 'п':
		case 'р':
		case 'с':
		case 'т':
		case 'у':
		case 'ф':
		case 'х':
		case 'ц':
		case 'ч':
		case 'ш':
		case 'щ':
		case 'ъ':
		case 'ы':
		case 'ь':
		case 'э':
		case 'ю':
		case 'я':
		case 'ѐ':
		case 'ё':
		case 'ђ':
		case 'ѓ':
		case 'є':
		case 'ѕ':
		case 'і':
		case 'ї':
		case 'ј':
		case 'љ':
		case 'њ':
		case 'ћ':
		case 'ќ':
		case 'ѝ':
		case 'ў':
		case 'џ':
		case 'Ѡ':
		case 'ѡ':
		case 'Ѣ':
		case 'ѣ':
		case 'Ѥ':
		case 'ѥ':
		case 'Ѧ':
		case 'ѧ':
		case 'Ѩ':
		case 'ѩ':
		case 'Ѫ':
		case 'ѫ':
		case 'Ѭ':
		case 'ѭ':
		case 'Ѯ':
		case 'ѯ':
		case 'Ѱ':
		case 'ѱ':
		case 'Ѳ':
		case 'ѳ':
		case 'Ѵ':
		case 'ѵ':
		case 'Ѷ':
		case 'ѷ':
		case 'Ѹ':
		case 'ѹ':
		case 'Ѻ':
		case 'ѻ':
		case 'Ѽ':
		case 'ѽ':
		case 'Ѿ':
		case 'ѿ':
		case 'Ҁ':
		case 'ҁ':
		case '҂':
		case 'Ҋ':
		case 'ҋ':
		case 'Ҍ':
		case 'ҍ':
		case 'Ҏ':
		case 'ҏ':
		case 'Ґ':
		case 'ґ':
		case 'Ғ':
		case 'ғ':
		case 'Ҕ':
		case 'ҕ':
		case 'Җ':
		case 'җ':
		case 'Ҙ':
		case 'ҙ':
		case 'Қ':
		case 'қ':
		case 'Ҝ':
		case 'ҝ':
		case 'Ҟ':
		case 'ҟ':
		case 'Ҡ':
		case 'ҡ':
		case 'Ң':
		case 'ң':
		case 'Ҥ':
		case 'ҥ':
		case 'Ҧ':
		case 'ҧ':
		case 'Ҩ':
		case 'ҩ':
		case 'Ҫ':
		case 'ҫ':
		case 'Ҭ':
		case 'ҭ':
		case 'Ү':
		case 'ү':
		case 'Ұ':
		case 'ұ':
		case 'Ҳ':
		case 'ҳ':
		case 'Ҵ':
		case 'ҵ':
		case 'Ҷ':
		case 'ҷ':
		case 'Ҹ':
		case 'ҹ':
		case 'Һ':
		case 'һ':
		case 'Ҽ':
		case 'ҽ':
		case 'Ҿ':
		case 'ҿ':
		case 'Ӏ':
		case 'Ӂ':
		case 'ӂ':
		case 'Ӄ':
		case 'ӄ':
		case 'Ӆ':
		case 'ӆ':
		case 'Ӈ':
		case 'ӈ':
		case 'Ӊ':
		case 'ӊ':
		case 'Ӌ':
		case 'ӌ':
		case 'Ӎ':
		case 'ӎ':
		case 'ӏ':
		case 'Ӑ':
		case 'ӑ':
		case 'Ӓ':
		case 'ӓ':
		case 'Ӕ':
		case 'ӕ':
		case 'Ӗ':
		case 'ӗ':
		case 'Ә':
		case 'ә':
		case 'Ӛ':
		case 'ӛ':
		case 'Ӝ':
		case 'ӝ':
		case 'Ӟ':
		case 'ӟ':
		case 'Ӡ':
		case 'ӡ':
		case 'Ӣ':
		case 'ӣ':
		case 'Ӥ':
		case 'ӥ':
		case 'Ӧ':
		case 'ӧ':
		case 'Ө':
		case 'ө':
		case 'Ӫ':
		case 'ӫ':
		case 'Ӭ':
		case 'ӭ':
		case 'Ӯ':
		case 'ӯ':
		case 'Ӱ':
		case 'ӱ':
		case 'Ӳ':
		case 'ӳ':
		case 'Ӵ':
		case 'ӵ':
		case 'Ӷ':
		case 'ӷ':
		case 'Ӹ':
		case 'ӹ':
		case 'Ӻ':
		case 'ӻ':
		case 'Ӽ':
		case 'ӽ':
		case 'Ӿ':
		case 'ӿ':
		case 'Ԁ':
		case 'ԁ':
		case 'Ԃ':
		case 'ԃ':
		case 'Ԅ':
		case 'ԅ':
		case 'Ԇ':
		case 'ԇ':
		case 'Ԉ':
		case 'ԉ':
		case 'Ԋ':
		case 'ԋ':
		case 'Ԍ':
		case 'ԍ':
		case 'Ԏ':
		case 'ԏ':
		case 'Ԑ':
		case 'ԑ':
		case 'Ԓ':
		case 'ԓ':
		case 'Ԕ':
		case 'ԕ':
		case 'Ԗ':
		case 'ԗ':
		case 'Ԙ':
		case 'ԙ':
		case 'Ԛ':
		case 'ԛ':
		case 'Ԝ':
		case 'ԝ':
		case 'Ԟ':
		case 'ԟ':
		case 'Ԡ':
		case 'ԡ':
		case 'Ԣ':
		case 'ԣ':
		case 'Ԥ':
		case 'ԥ':
		case 'Ԧ':
		case 'ԧ':
		case 'Ԩ':
		case 'ԩ':
		case 'Ԫ':
		case 'ԫ':
		case 'Ԭ':
		case 'ԭ':
		case 'Ԯ':
		case 'ԯ':
		case 'Ա':
		case 'Բ':
		case 'Գ':
		case 'Դ':
		case 'Ե':
		case 'Զ':
		case 'Է':
		case 'Ը':
		case 'Թ':
		case 'Ժ':
		case 'Ի':
		case 'Լ':
		case 'Խ':
		case 'Ծ':
		case 'Կ':
		case 'Հ':
		case 'Ձ':
		case 'Ղ':
		case 'Ճ':
		case 'Մ':
		case 'Յ':
		case 'Ն':
		case 'Շ':
		case 'Ո':
		case 'Չ':
		case 'Պ':
		case 'Ջ':
		case 'Ռ':
		case 'Ս':
		case 'Վ':
		case 'Տ':
		case 'Ր':
		case 'Ց':
		case 'Ւ':
		case 'Փ':
		case 'Ք':
		case 'Օ':
		case 'ա':
		case 'բ':
		case 'գ':
		case 'դ':
		case 'ե':
		case 'զ':
		case 'է':
		case 'ը':
		case 'թ':
		case 'ժ':
		case 'ի':
		case 'լ':
		case 'խ':
		case 'ծ':
		case 'կ':
		case 'հ':
		case 'ձ':
		case 'ղ':
		case 'ճ':
		case 'մ':
		case 'յ':
		case 'ն':
		case 'շ':
		case 'ո':
		case 'չ':
		case 'պ':
		case 'ջ':
		case 'ռ':
		case 'ս':
		case 'վ':
		case 'տ':
		case 'ր':
		case 'ց':
		case 'ւ':
		case 'փ':
		case 'ք':
		case 'օ':
		case 'ֆ':
		case 'և':
		case '-':
		case '*':
		    buffer.append(copy.charAt(i));
		    break;

		// throw an exception for anything else
		default:
		    throw new SequenceParserException("Invalid sequence character: '" + copy.charAt(i) + "'");
	    }
	}
	return buffer.toString();
    }
}