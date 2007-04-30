package korat.gui.viz.metamodel;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * <ul>
 * <li>Factory for the <code>AlloySig</code> class.</li>
 * <li>Keeps tracks of all created <code>AlloySig</code> objects.</li>
 * <li>Ensures that the names of all <code>AlloySig</code> objects are
 * unique.</li>
 * </ul>
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
public class SigFactory {

    private static SigFactory instance = new SigFactory();

    public static SigFactory getInstance() {
        return instance;
    }

    private SigFactory() {

    }

    private Map<String, AlloySig> allSigs = new HashMap<String, AlloySig>();

    public Collection<AlloySig> getAllSigs() {
        return allSigs.values();
    }

    public AlloySig getAlloySig(String name) {

        AlloySig sig = null;

        try {
            sig = getAlloySig(name, null);
        } catch (SigDoesNotExistException e) {
        }

        return sig;

    }

    public AlloySig getAlloySig(String name, String extendsName)
            throws SigDoesNotExistException {

        AlloySig sig = allSigs.get(name);

        if (sig == null) {

            sig = new AlloySig(name);

            if (extendsName != null) {

                AlloySig extendsSig = allSigs.get(extendsName);
                if (extendsSig == null)
                    throw new SigDoesNotExistException(extendsName,
                            "\"Extends\" sig doesn't exist.");
                sig.setExtendsSig(extendsSig);

            }

            allSigs.put(name, sig);

        }

        return sig;

    }

    public void clear() {
        allSigs.clear();
    }

}
