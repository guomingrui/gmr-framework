package gmr.all.framework.factory;

import gmr.all.framework.Session;

public class SessionFactory {
	private static ThreadLocal<Session> threadLocal = new ThreadLocal<Session>();
	
	public static Session getCurrentSession(){
		Session session = threadLocal.get();
		if(session==null){
			session = new Session();
			threadLocal.set(session);
		}
		return session;
	}
}
