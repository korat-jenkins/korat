package korat.utils;

import korat.testing.ITestCaseListener;
import korat.utils.io.ISerializer;
import korat.utils.io.SimpleSerializer;

public class SerializationListener implements ITestCaseListener {

    private ISerializer serializer;

    public SerializationListener(String testsName) {
        this.serializer = new SimpleSerializer(testsName);
    }

    public void notifyNewTestCase(Object testCase) {
        serializer.serialize(testCase);
    }

    public void notifyTestFinished(long numOfExplored, long numOfGenerated) {
        
    }

}
