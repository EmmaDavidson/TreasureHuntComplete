package sqlLiteDatabase;
import java.sql.Date;

public class MapData {

	private Date mStartDate;
	private int participantId;
	
	public MapData()
	{
		participantId = -1;
		mStartDate = new Date(System.currentTimeMillis());
	}

	public Date getStartDate()
	{
		return mStartDate;
	}
	
	public void setStartDate(Date start)
	{
		mStartDate = start;
	}

	
	public int getParticipantId()
	{
		return participantId;
	}
	
	public void setParticipantId(int id)
	{
		 participantId = id;
	}
	
	
	public int getDurationSeconds(long endMillis)
	{
		return (int)((endMillis - mStartDate.getTime()) / 1000);
	}
	
	public static String formatDuration(int durationSeconds)
	{
		int seconds = durationSeconds%60;
		int minutes = ((durationSeconds - seconds) / 60) % 60;
		int hours = (durationSeconds - (minutes * 60) - seconds) / 3600;
		return String.format("%02d:%02d:%02d", hours, minutes, seconds);
		
	}
}
