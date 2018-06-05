package com.perforce.team.core.p4java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.eclipse.core.runtime.Assert;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.IStream;
import com.perforce.p4java.core.IStreamSummary;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.team.core.IConstants;

public class P4Stream extends P4Container implements IP4Stream {

	private IP4Connection connection;
	private IStreamSummary streamSummary;
	List<IP4Stream> children = new ArrayList<IP4Stream>();

	// //// P4Resource /////////
	public String getName() {
		return getStreamSummary().getName();
	}

	public String getLocalPath() {
		return null;
	}

	public String getClientPath() {
		return null;
	}

	public String getRemotePath() {
		return null;
	}

	public String getActionPath() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getActionPath(Type preferredType) {
		// TODO Auto-generated method stub
		return null;
	}

	public IP4Container getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	public IClient getClient() {
		IClient client = null;
		if (this.connection != null) {
			client = this.connection.getClient();
		}
		return client;
	}

	public IP4Connection getConnection() {
		return this.connection;
	}

	@Override
	public void refresh(int depth) {
		// TODO Auto-generated method stub

	}

	@Override
	public void refresh() {
		try {
			fetchDetail();
		} catch (P4JavaException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isContainer() {
		// TODO Auto-generated method stub
		return false;
	}

	// ///////////// New method //////////////////

	public P4Stream(IP4Connection connection, IStreamSummary streamSummary) {
		super();
		Assert.isNotNull(connection);
		Assert.isNotNull(streamSummary);
		this.connection = connection;
		this.streamSummary = streamSummary;
	}

	private P4Stream() {
	}

	@Override
	public boolean needsRefresh() {
	    if(this.streamSummary instanceof IStream)
	        return false;
	    return super.needsRefresh();
	}
	
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		if (adapter == IStreamSummary.class) {
			return this.streamSummary;
		} else if (adapter == IStream.class) {
			if (this.streamSummary instanceof IStream) {
				return this.streamSummary;
			} else if (this.streamSummary != null) {
				try {
					return fetchDetail();
				} catch (P4JavaException e) {
					e.printStackTrace();
				}
			}
		} else if (adapter == IP4Stream.class){
		    return this;
		}
		return super.getAdapter(adapter);
	}

	public IStreamSummary getStreamSummary() {
		return streamSummary;
	}

	public IStream fetchDetail() throws P4JavaException {
		this.streamSummary=connection.getStreamSummary(this.streamSummary.getStream());
		return (IStream) this.streamSummary;
	}

    public List<String> getClientView() {
    	return connection.getStreamViewMapping(streamSummary.getStream());
    }

	@Override
	public boolean equals(Object obj) {
		boolean eq = super.equals(obj);
		if (!eq && obj != null && obj instanceof IP4Stream) {
			IStreamSummary sum = ((IP4Stream) obj).getStreamSummary();
			IP4Connection conn = ((IP4Stream) obj).getConnection();
			if (P4Connection.testEqual(conn, this.getConnection())) {
				if (sum == this.getStreamSummary())
					return true;
				if (sum != null && this.getStreamSummary() != null) {
					return sum.getStream().equals(
							this.getStreamSummary().getStream());
				}
			}
		}
		return eq;
	}

	@Override
	public int hashCode() {
		StringBuilder sb = new StringBuilder();
		if (this.getConnection() != null)
			sb.append(this.getConnection().getName());
		if (this.getStreamSummary() != null) {
			String streamId = this.getStreamSummary().getStream();
			if (streamId != null && sb.length() > 0) {
				sb.append(streamId);
				return sb.toString().hashCode();
			}
		}
		return super.hashCode();
	}

	public static boolean testEquals(IP4Stream s1, IP4Stream s2) {
		return EqualsBuilder.reflectionEquals(s1, s2);
	}

	public static String toString(String indent, IP4Stream stream) {
		IStreamSummary summary = stream.getStreamSummary();
		StringBuilder sb = new StringBuilder();
		sb.append(indent + "stream="
				+ (summary != null ? summary.getStream() : null)
				+ IConstants.RETURN);
		final String DELTA = "  ";
		for (IP4Resource child : stream.members()) {
			Assert.isTrue(child instanceof IP4Stream);
			sb.append(P4Stream.toString(indent + DELTA, ((IP4Stream) child)));
		}
		return sb.toString();
	}

	@Override
	public IP4Resource[] members() {
		return getChildren().toArray(new P4Stream[0]);
	}

	public List<IP4Stream> getChildren() {
		return children;
	}
	
	public static VirturalRoot constructTree(List<IP4Stream> summaries,
			StreamCache cache) {
		VirturalRoot root = new VirturalRoot();
		for (IP4Stream sum : summaries) {
			constructTreeNode(sum, cache, root);
		}
		return root;
	}

	private static IP4Stream constructTreeNode(IP4Stream sum,
			StreamCache cache, VirturalRoot root) {
        String pstream = sum.getStreamSummary().getParent();
        String stream=sum.getStreamSummary().getStream();
        IP4Stream p = cache.get(sum.getConnection(), pstream);
        
        IP4Stream node=null;
        
        IP4Stream pnode=root;
        if(p!=null){
            pnode = constructTreeNode(p, cache, root);
        }
        node = root.get(stream);
        if(node==null){
            node = new P4Stream(sum.getConnection(), sum.getStreamSummary());
            pnode.getChildren().add(node);
            root.put(stream, node);
        }
        return node;

	}

	public static class VirturalRoot extends P4Stream {
        Map<String, IP4Stream> map=new HashMap<String, IP4Stream>();
		public VirturalRoot() {}
		
        public void clear(){
            map.clear();
        }
        
        public IP4Stream get(String stream){
            return map.get(stream);
        }
        public void put(String stream, IP4Stream node){
            map.put(stream, node);
        }
        
        @Override
        public boolean equals(Object obj) {
        	// override to prevent coverity from complaining.
        	return obj instanceof VirturalRoot && super.equals(obj);
        }
        
        @Override
        public int hashCode() {
        	// override to prevent coverity from complaining: FB.EQ_DOESNT_OVERRIDE_EQUALS
        	return super.hashCode();
        }
		
	}

	/** A stream cache keyed by connection and stream id.*/
	public static class StreamCache {
	    private Map<IP4Connection, Map<String,IP4Stream>> cache=Collections
	            .synchronizedMap(new HashMap<IP4Connection, Map<String,IP4Stream>>());

	    public void clear(IP4Connection connection){
	    	if(connection!=null){
		        Map<String, IP4Stream> map = cache.remove(connection);
		        if(map!=null)
		            map.clear();
	    	}
	    }
	    
	    public void add(IP4Connection connection, List<IP4Stream> streams){
	        Map<String, IP4Stream> map = cache.get(connection);
	        if(map==null){
	            map=new HashMap<String, IP4Stream>();
	            cache.put(connection, map);
	        }
	        for(IP4Stream sum: streams){
	            map.put(sum.getStreamSummary().getStream(), sum);
	        }
	    }
	    
	    public void add(IP4Stream sum){
	        Map<String, IP4Stream> map = cache.get(sum.getConnection());
	        if(map==null){
	            map=new HashMap<String, IP4Stream>();
	            cache.put(sum.getConnection(), map);
	        }
	        map.put(sum.getStreamSummary().getStream(), sum);
	    }
	    
	    public IP4Stream get(IP4Connection connection, String streamId){
	        Map<String, IP4Stream> map = cache.get(connection);
	        if(map!=null)
	            return map.get(streamId);
	        return null;
	        
	    }
	    
	    public IP4Stream[] getSummaries(IP4Connection connection){
	        return cache.get(connection).values().toArray(new IP4Stream[0]);
	    }

	    public boolean isEmpty(IP4Connection conn) {
	        Map<String, IP4Stream> map = cache.get(conn);
	        return (map==null || map.isEmpty());
	    }
	}

}
