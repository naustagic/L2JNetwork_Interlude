package net.sf.l2j.gameserver;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Logger;

import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.Config;

public class Restart
{
	public static final Logger _log = Logger.getLogger(Restart.class.getName());
	private Calendar NextRestart;
	private SimpleDateFormat format = new SimpleDateFormat("HH:mm");
	
	public String getRestartNextTime()
	{
		if (NextRestart.getTime() != null)
			return format.format(NextRestart.getTime());
		return "Erro";
	}
	
	public void StartCalculationOfNextRestartTime()
	{
		_log.info("#####################################");
		_log.info("#[Restart System]: System actived...#");
		_log.info("#####################################");
		try
		{
			Calendar currentTime = Calendar.getInstance();
			Calendar testStartTime = null;
			long flush2 = 0, timeL = 0;
			int count = 0;
			
			for (String timeOfDay : Config.RESTART_INTERVAL_BY_TIME_OF_DAY)
			{
				testStartTime = Calendar.getInstance();
				testStartTime.setLenient(true);
				String[] splitTimeOfDay = timeOfDay.split(":");
				testStartTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(splitTimeOfDay[0]));
				testStartTime.set(Calendar.MINUTE, Integer.parseInt(splitTimeOfDay[1]));
				testStartTime.set(Calendar.SECOND, 00);
				// Verifica a validade to tempo
				if (testStartTime.getTimeInMillis() < currentTime.getTimeInMillis())
				{
					testStartTime.add(Calendar.DAY_OF_MONTH, 1);
				}
				
				// TimeL Recebe o quanto falta de milisegundos para o restart
				timeL = testStartTime.getTimeInMillis() - currentTime.getTimeInMillis();
				
				// Verifica qual horario sera o proximo restart
				if (count == 0)
				{
					flush2 = timeL;
					NextRestart = testStartTime;
				}
				
				if (timeL < flush2)
				{
					flush2 = timeL;
					NextRestart = testStartTime;
				}
				
				count++;
			}
			_log.info("[AutoRestart]: Next Restart Time: " + NextRestart.getTime().toString());
			ThreadPool.schedule(new StartRestartTask(), flush2);
		}
		catch (Exception e)
		{
			System.out.println("[AutoRestart]: The restart automated server presented error in load restarts period config !");
		}
	}
	
	class StartRestartTask implements Runnable
	{
		@Override
		public void run()
		{
			_log.info("Start automated restart GameServer.");
			Shutdown.getInstance().autoRestart(Config.RESTART_SECONDS);
		}
	}
	
	
	public static Restart getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final Restart _instance = new Restart();
	}
}