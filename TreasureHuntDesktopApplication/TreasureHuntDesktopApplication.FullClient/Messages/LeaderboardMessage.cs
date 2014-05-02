using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using TreasureHuntDesktopApplication.FullClient.TreasureHuntService;

//----------------------------------------------------------
//<copyright>
//Emma Davidson - Treasure Hunt 2013-3014 Final Year Project
//</copyright>
//----------------------------------------------------------

namespace TreasureHuntDesktopApplication.FullClient.Messages
{
    /// <Summary>  The purpose of this class is to be a framework for sending messages and data about the current hunt in relation to 
    /// viewing a leader board for that particular hunt.</Summary> 

    public class LeaderboardMessage
    {
        public hunt CurrentHunt { get; set; }
    }
}
