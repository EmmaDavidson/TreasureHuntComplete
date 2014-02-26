using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace TreasureHuntDesktopApplication.FullClient.Utilities
{
    public class Participant
    {
        public String Name { get; set; }
        public double Tally { get; set; }
        public float ElapsedTime { get; set; }

        public Participant(String name, double tally, float time)
        {
            this.Name = name;
            this.Tally = tally;
            this.ElapsedTime = time;
        }

      /*  public void setName(String name)
        {
            this.userName = name;
        }

        public void setTally(double tallyValue)
        {
            this.tally = tallyValue;
        }

        public void setElapsedTime(float time)
        {
            this.elapsedTime = time;
        }

        public String getName()
        {
            return this.userName;
        }

        public int getTally()
        {
            return this.tally;
        }

        public float getElapsedTime()
        {
            return this.elapsedTime;
        } */
    }
}
