/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IP4CommandListener {

    /**
     * Logs an info line
     * 
     * @param id command identifier
     * @param line
     */
    void info(int id, String line);

    /**
     * Logs an error line
     * 
     * @param id command identifier
     * @param line
     */
    void error(int id, String line);

    /**
     * Logs a command lien
     * 
     * @param id command identifier
     * @param line
     */
    void command(int id, String line);
    
    public static class P4CommandAdapter implements IP4CommandListener{

		@Override
		public void info(int id, String line) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void error(int id, String line) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void command(int id, String line) {
			// TODO Auto-generated method stub
			
		}

    }

}
