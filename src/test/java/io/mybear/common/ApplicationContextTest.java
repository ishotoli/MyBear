package io.mybear.common;

import junit.framework.TestCase;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationContextTest extends TestCase{
    Logger logger = LoggerFactory.getLogger(ApplicationContext.class);

    private ApplicationContext context;

    @Override
    protected void setUp() throws Exception {
        context = new ApplicationContext("config-test.properties", "config-test2.properties");
        logger.debug("context: {}", context);
    }

    @Test
    public void testGetProperty(){
        assertEquals(context.getProperty("a"), "x,y,z");
    }
}
