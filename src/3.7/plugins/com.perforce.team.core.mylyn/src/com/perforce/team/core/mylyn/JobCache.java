/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mylyn;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Job;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class JobCache {

    /**
     * CACHE_SIZE
     */
    public static final int CACHE_SIZE = 256;

    private static class LimitMap<K, V> extends LinkedHashMap<K, V> {

        /**
         * serialVersionUID
         */
        private static final long serialVersionUID = 6187696521678349632L;

        /**
         * @see java.util.LinkedHashMap#removeEldestEntry(java.util.Map.Entry)
         */
        @Override
        protected boolean removeEldestEntry(Entry<K, V> eldest) {
            return size() > CACHE_SIZE;
        }

    }

    private Map<IP4Connection, LimitMap<String, IP4Job>> allJobs = new HashMap<IP4Connection, LimitMap<String, IP4Job>>();

    /**
     * Add job to cache
     * 
     * @param job
     */
    public void add(IP4Job job) {
        if (job != null) {
            IP4Connection connection = job.getConnection();
            String id = job.getId();
            LimitMap<String, IP4Job> jobs = this.allJobs.get(connection);
            if (jobs == null) {
                jobs = new LimitMap<String, IP4Job>();
                this.allJobs.put(connection, jobs);
            }
            jobs.put(id, job);
        }
    }

    /**
     * Get job from cache
     * 
     * @param id
     * @param connection
     * @return - job or null if not in cache
     */
    public IP4Job get(String id, IP4Connection connection) {
        IP4Job job = null;
        if (id != null && connection != null) {
            LimitMap<String, IP4Job> jobs = this.allJobs.get(connection);
            if (jobs != null) {
                job = jobs.get(id);
            }
        }
        return job;
    }

    /**
     * Clear the cache
     */
    public void clear() {
        this.allJobs.clear();
    }

}
