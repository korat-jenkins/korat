package korat.config;

import org.apache.commons.cli.Option;

/**
 * Just like org.apache.commons.cli.Option with default value
 * for the option's argument in addition.
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
public class MyOption extends Option {

    protected String argDefValue;
    
    public MyOption(String opt, String longOpt, String description, boolean isRequired,
            boolean hasArg, String argName, String argDefValue) throws IllegalArgumentException {
        super(opt, description);
        setLongOpt(longOpt);
        setRequired(false);
        if (hasArg)
            setArgs(1);
        else
            setArgs(0);
        setArgName(argName);
        this.argDefValue = argDefValue;
    }

    @Override
    public Object clone() {
        MyOption option = new MyOption(getOpt(), getLongOpt(), getDescription(), 
                isRequired(), hasArg(), getArgName(), argDefValue);
        option.setArgs( getArgs() );
        option.setOptionalArg( hasOptionalArg() );
        option.setType( getType() );
        option.setValueSeparator( getValueSeparator() );
        return option;
    }

    @Override
    public String toString() {
        return getLongOpt();
    }
    
    public String getSwitches() {
        return "-" + getOpt() + " (--" + getLongOpt() + ")";
    }
}