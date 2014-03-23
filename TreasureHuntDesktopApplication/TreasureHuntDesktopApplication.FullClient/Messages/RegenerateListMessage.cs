using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

//----------------------------------------------------------
//<copyright>
//</copyright>
//----------------------------------------------------------

namespace TreasureHuntDesktopApplication.FullClient.Messages
{
    /// <Summary> The purpose of this class is to be a framework for sending messages and data about whether of not a list should be
    /// regenerated. </Summary>

    public class RegenerateListMessage
    {
        public bool RegenerateList { get; set; }
    }
}
