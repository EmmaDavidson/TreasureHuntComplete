using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

//----------------------------------------------------------
//<copyright>
//Emma Davidson - Treasure Hunt 2013-3014 Final Year Project
//</copyright>
//----------------------------------------------------------

namespace TreasureHuntDesktopApplication.FullClient.Utilities
{
   /// <Summary>  The purpose of this class is to represent participant data. It is for storing an accumulation 
   /// of different data (from the database) for a particular participant so it can be displayed for use on a leader board.</Summary>
    public class Participant
    {
        /// <Summary> Participant variables </Summary>
        public String Name { get; set; }
        public double Tally { get; set; }
        public float ElapsedTime { get; set; }

        /// <Summary> Constructor </Summary>
        public Participant(String name, double tally, float time)
        {
            this.Name = name;
            this.Tally = tally;
            this.ElapsedTime = time;
        }
    }
}
