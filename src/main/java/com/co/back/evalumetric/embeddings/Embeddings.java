package com.co.back.evalumetric.embeddings;

import com.co.back.evalumetric.data.RelationalTrace;
import com.co.back.evalumetric.evaluation.metrics.impl.UkkonenSuffixTree;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.xes.model.XEvent;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Embeddings {

    private static final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿĀāĂăĄąĆćĈĉĊċČčĎďĐđĒēĔĕĖėĘęĚěĜĝĞğĠġĢģĤĥĦħĨĩĪīĬĭĮįİıĲĳĴĵĶķĸĹĺĻļĽľĿŀŁłŃńŅņŇňŉŊŋŌōŎŏŐőŒœŔŕŖŗŘřŚśŜŝŞşŠšŢţŤťŦŧŨũŪūŬŭŮůŰűŲųŴŵŶŷŸŹźŻżŽžſƀƁƂƃƄƅƆƇƈƉƊƋƌƍƎƏƐƑƒƓƔƕƖƗƘƙƚƛƜƝƞƟƠơƢƣƤƥƦƧƨƩƪƫƬƭƮƯưƱƲƳƴƵƶƷƸƹƺƻƼƽƾƿǀǁǂǃΑΒΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡΣΤΥΦΧΨΩΪΫάέήίΰαβγδεζηθικλμνξοπρςστυφχψωϊϋόύώϏϐϑϒϓϔϕϖϗϘϙϚϛϜϝϞϟϠϡϢϣϤϥϦϧϨϩϪϫϬϭϮϯϰϱϲϳϴϵ϶ϷϸϹϺϻϼϽϾϿЀЁЂЃЄЅІЇЈЉЊЋЌЍЎЏАБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдежзийклмнопрстуфхцчшщъыьэюяѐёђѓєѕіїјљњћќѝўџѠѡѢѣѤѥѦѧѨѩѪѫѬѭѮѯѰѱѲѳѴѵѶѷѸѹѺѻѼѽѾѿҀҁ҂ҊҋҌҍҎҏҐґҒғҔҕҖҗҘҙҚқҜҝҞҟҠҡҢңҤҥҦҧҨҩҪҫҬҭҮүҰұҲҳҴҵҶҷҸҹҺһҼҽҾҿӀӁӂӃӄӅӆӇӈӉӊӋӌӍӎӏӐӑӒӓӔӕӖӗӘәӚӛӜӝӞӟӠӡӢӣӤӥӦӧӨөӪӫӬӭӮӯӰӱӲӳӴӵӶӷӸӹӺӻӼӽӾӿԀԁԂԃԄԅԆԇԈԉԊԋԌԍԎԏԐԑԒԓԔԕԖԗԘԙԚԛԜԝԞԟԠԡԢԣԤԥԦԧԨԩԪԫԬԭԮԯԱԲԳԴԵԶԷԸԹԺԻԼԽԾԿՀՁՂՃՄՅՆՇՈՉՊՋՌՍՎՏՐՑՒՓՔՕաբգդեզէըթժիլխծկհձղճմյնշոչպջռսվտրցւփքօֆև-*";

    private static ConcurrentMap<String, Character>                                                           activityCharMap          = new ConcurrentHashMap<>();
    private static ConcurrentMap<RelationalTrace<XEvent, ?>, ConcurrentMap<String, BigDecimal>>               cachedEmbeddingsBOAtt    = new ConcurrentHashMap<>();
    private static ConcurrentMap<RelationalTrace<XEvent, ?>, ConcurrentMap<String, BigDecimal>>               cachedEmbeddingsBOAct    = new ConcurrentHashMap<>();
    private static ConcurrentMap<RelationalTrace<XEvent, ?>, ConcurrentMap<String, BigDecimal>>               cachedEmbeddingsBOActBin = new ConcurrentHashMap<>();
    private static ConcurrentMap<RelationalTrace<XEvent, ?>, ConcurrentMap<Pair<String, String>, BigDecimal>> cachedEmbeddingsDFG      = new ConcurrentHashMap<>();
    private static ConcurrentMap<String, ConcurrentMap<String, Integer>>                                      cachedEmbeddingsEF       = new ConcurrentHashMap<>();
    private static ConcurrentMap<RelationalTrace<XEvent, ?>, ConcurrentMap<String, BigDecimal>>               cachedEmbeddingsNGram    = new ConcurrentHashMap<>();
    private static ConcurrentMap<RelationalTrace<XEvent, ?>, ConcurrentMap<String, BigDecimal>>               cachedEmbeddingsNGramAll = new ConcurrentHashMap<>();
    private static ConcurrentMap<RelationalTrace<XEvent, ?>, ConcurrentMap<String, BigDecimal>>               cachedEmbeddingsMR       = new ConcurrentHashMap<>();
    private static ConcurrentMap<RelationalTrace<XEvent, ?>, ConcurrentMap<String, BigDecimal>>               cachedEmbeddingsNSMR     = new ConcurrentHashMap<>();
    private static ConcurrentMap<RelationalTrace<XEvent, ?>, ConcurrentMap<String, BigDecimal>>               cachedEmbeddingsSMR      = new ConcurrentHashMap<>();
    private static ConcurrentMap<List<XEvent>, String>                                                        cachedStringEmbeddings   = new ConcurrentHashMap<>();

    public static ConcurrentMap<String, BigDecimal> bagOfAttributes ( final RelationalTrace<XEvent, ?> t, final Set<String> omittedAttributes ) {

	return cachedEmbeddingsBOAtt.computeIfAbsent(t, f -> {

	    final ConcurrentMap<String, BigDecimal> result = new ConcurrentHashMap<>();

	    t.getEvents().stream().forEach(e -> e.getAttributes().entrySet().forEach(a -> {
		if ( !omittedAttributes.contains(a.getKey()) ) {
		    result.compute(a.getKey() + a.getValue().toString(), ( k, v ) -> v == null ? BigDecimal.ONE : v.add(BigDecimal.ONE));
		}
	    }));

	    return result;
	});
    }

    public static ConcurrentMap<String, BigDecimal> bagOfWords ( final RelationalTrace<XEvent, ?> t ) {

	final String eventClassifier = t.getEventClassifier();

	return cachedEmbeddingsBOAct.computeIfAbsent(t, f -> {

	    final ConcurrentMap<String, BigDecimal> result = new ConcurrentHashMap<>();

	    t.getEvents().stream().forEach(e -> result.compute(!e.getAttributes().containsKey(eventClassifier) ? "NULL" : e.getAttributes().get(eventClassifier).toString(), ( k, v ) -> v == null ? BigDecimal.ONE : v.add(BigDecimal.ONE)));

	    return result;
	});
    }

    public static ConcurrentMap<Pair<String, String>, BigDecimal> directlyFollows ( final RelationalTrace<XEvent, ?> t ) {

	final String eventClassifier = t.getEventClassifier();

	return cachedEmbeddingsDFG.computeIfAbsent(t, f -> {

	    final ConcurrentMap<Pair<String, String>, BigDecimal> result = new ConcurrentHashMap<>();

	    String previousEventName = null;

	    for ( XEvent e : t.getTotalOrder() ) {

		String currentEventName = !e.getAttributes().containsKey(eventClassifier) ? "NULL" : e.getAttributes().get(eventClassifier).toString();

		if ( previousEventName == null ) {
		    previousEventName = currentEventName;
		} else {
		    result.compute(new ImmutablePair<>(previousEventName, currentEventName), ( k, v ) -> v == null ? BigDecimal.ONE : v.add(BigDecimal.ONE));
		}
	    }

	    return result;
	});
    }

    public static Map<String, Integer> followsDistance ( final RelationalTrace<XEvent, ?> t, final String eventClassifier ) {

	String tAsStr = traceToString(t, eventClassifier);

	return cachedEmbeddingsEF.computeIfAbsent(tAsStr, f -> {

	    final ConcurrentMap<String, Integer> result = new ConcurrentHashMap<>();

	    for ( int i = 0; i < t.getTotalOrder().size(); i++ ) {

		XEvent e1    = t.getTotalOrder().get(i);
		String first = !e1.getAttributes().containsKey(eventClassifier) ? "NULL" : e1.getAttributes().get(eventClassifier).toString();

		for ( int j = i + 1; j < t.getTotalOrder().size(); j++ ) {

		    XEvent e2     = t.getTotalOrder().get(j);
		    String second = !e2.getAttributes().containsKey(eventClassifier) ? "NULL" : e2.getAttributes().get(eventClassifier).toString();

		    String p = first + second;

		    if ( result.get(p) == null || result.get(p) > ( j - i ) ) {
			result.put(p, j - i);
		    }
		}
	    }

	    return result;
	});
    }

    public static ConcurrentMap<String, BigDecimal> bagOfWordsBinary ( final RelationalTrace<XEvent, ?> t ) {

	final String eventClassifier = t.getEventClassifier();

	return cachedEmbeddingsBOActBin.computeIfAbsent(t, f -> {

	    final ConcurrentMap<String, BigDecimal> result = new ConcurrentHashMap<>();

	    t.getEvents().stream().forEach(e -> result.compute(!e.getAttributes().containsKey(eventClassifier) ? "NULL" : e.getAttributes().get(eventClassifier).toString(), ( k, v ) -> v == null ? BigDecimal.ONE : BigDecimal.ONE));

	    return result;
	});
    }

    synchronized public static String traceToString ( final RelationalTrace<XEvent, ?> t ) {

	return traceToString(t, t.getEventClassifier());
    }

    synchronized public static String traceToString ( final RelationalTrace<XEvent, ?> t, final String eventClassifier ) {

	if (t.getTotalOrderStr() == null) {
	    t.setTotalOrderStr(traceToString(t, eventClassifier, 0, t.getTotalOrder().size()));
	}

	return t.getTotalOrderStr();
    }

    synchronized public static String traceToString ( final RelationalTrace<XEvent, ?> t, final String eventClassifier, int fromIndex, int toIndex ) {

	return cachedStringEmbeddings.computeIfAbsent(t.getTotalOrder().subList(fromIndex, toIndex), u -> {

	    StringBuilder result = new StringBuilder();

	    for ( int i = fromIndex; i < toIndex; i++ ) {

		XEvent e = t.getTotalOrder().get(i);

		String activity = !e.getAttributes().containsKey(eventClassifier) ? "NULL" : e.getAttributes().get(eventClassifier).toString();

		if ( activityCharMap.size() >= chars.length() ) {
		    System.out.println("ran out of chars");
		    throw new IllegalArgumentException("ran out of chars!");
		} else {
		    Character activityAsChar = activityCharMap.computeIfAbsent(activity, v -> new Character(chars.charAt(activityCharMap.size())));
		    result.append(activityAsChar);
		}
	    }

	    return result.toString();
	});
    }

    public static ConcurrentMap<String, BigDecimal> maximalRepeats ( final RelationalTrace<XEvent, ?> t, final String eventClassifier ) {

	if ( t.getTotalOrder().size() < 2 ) {
	    //System.out.println("Trace length: " + t.getTotalOrder().size());
	    return new ConcurrentHashMap<>();
	} else {

	    return cachedEmbeddingsMR.computeIfAbsent(t, v -> {

		ConcurrentMap<String, BigDecimal> result     = new ConcurrentHashMap<>();
		String                            s          = Embeddings.traceToString(t, eventClassifier);
		UkkonenSuffixTree                 suffixTree = new UkkonenSuffixTree(1, s);
		suffixTree.findLeftDiverseNodes();
		Set<String> mr = suffixTree.getMaximalRepeats();

        /*
        if (mr == null) {
            for ( XEvent e : t.getTotalOrder() ) {
                e.getAttributes().entrySet().forEach(a -> System.out.println(a.getKey() + ": " + a.getValue()));
            }
            System.out.println(",");
        }
        */

		mr.forEach(r -> result.put(r, new BigDecimal(suffixTree.getMatches(r).length)));

		return result;
	    });
	}
    }

    public static ConcurrentMap<String, BigDecimal> superMaximalRepeats ( final RelationalTrace<XEvent, ?> t, final String eventClassifier ) {

	if ( t.getTotalOrder().size() < 2 ) {
	    //System.out.println("Trace length: " + t.getTotalOrder().size());
	    return new ConcurrentHashMap<>();
	} else {

	    return cachedEmbeddingsSMR.computeIfAbsent(t, v -> {
		ConcurrentMap<String, BigDecimal> result     = new ConcurrentHashMap<>();
		String                            s          = Embeddings.traceToString(t, eventClassifier);
		UkkonenSuffixTree                 suffixTree = new UkkonenSuffixTree(1, s);

		suffixTree.findLeftDiverseNodes();

		Set<String> mr = suffixTree.getSuperMaximalRepeats();

		mr.forEach(r -> result.put(r, new BigDecimal(suffixTree.getMatches(r).length)));

		return result;
	    });
	}
    }

    public static ConcurrentMap<String, BigDecimal> nearSuperMaximalRepeats ( final RelationalTrace<XEvent, ?> t, final String eventClassifier ) {

	if ( t.getTotalOrder().size() < 2 ) {
	    //System.out.println("Trace length: " + t.getTotalOrder().size());
	    return new ConcurrentHashMap<>();
	} else {

	    return cachedEmbeddingsNSMR.computeIfAbsent(t, v -> {

		ConcurrentMap<String, BigDecimal> result     = new ConcurrentHashMap<>();
		String                            s          = Embeddings.traceToString(t, eventClassifier);
		UkkonenSuffixTree                 suffixTree = new UkkonenSuffixTree(1, s);

		suffixTree.findLeftDiverseNodes();

		Set<String> mr = suffixTree.getNearSuperMaximalRepeats();

		mr.forEach(r -> result.put(r, new BigDecimal(suffixTree.getMatches(r).length)));

		return result;
	    });
	}
    }

    public static ConcurrentMap<String, BigDecimal> nGram ( final RelationalTrace<XEvent, ?> t, int n ) {

	final String eventClassifier = t.getEventClassifier();

	return cachedEmbeddingsNGram.computeIfAbsent(t, f -> {

	    final ConcurrentMap<String, BigDecimal> result = new ConcurrentHashMap<>();

	    for ( int i = 0; i <= t.getTotalOrder().size() - n; i++ ) {
		String nGram = traceToString(t, eventClassifier, i, i + n);
		result.compute(nGram, ( k, v ) -> v == null ? BigDecimal.ONE : v.add(BigDecimal.ONE));
	    }
	    return result;
	});
    }

    public static ConcurrentMap<String, BigDecimal> nGramAll ( final RelationalTrace<XEvent, ?> t ) {

	return nGramAll(t, t.getTotalOrder().size());
    }

    public static ConcurrentMap<String, BigDecimal> nGramAll ( final RelationalTrace<XEvent, ?> t, int maxN ) {

	final String eventClassifier = t.getEventClassifier();

	return cachedEmbeddingsNGramAll.computeIfAbsent(t, f -> {

	    final ConcurrentMap<String, BigDecimal> result = new ConcurrentHashMap<>();

	    for ( int n = 1; n <= maxN; n++ ) {

		for ( int i = 0; i <= t.getTotalOrder().size() - n; i++ ) {
		    String nGram = traceToString(t, eventClassifier, i, i + n);
		    result.compute(nGram, ( k, v ) -> v == null ? BigDecimal.ONE : v.add(BigDecimal.ONE));
		}
	    }

	    return result;
	});
    }
}
