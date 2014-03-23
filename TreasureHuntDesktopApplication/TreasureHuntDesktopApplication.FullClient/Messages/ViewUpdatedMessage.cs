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
    /// <Summary> The purpose of this class is to be a framework for sending messages and data about whether or not 
    /// the on screen view has been updated. </Summary>

    public class ViewUpdatedMessage
    {
        public bool UpdatedView { get; set; }
    }
}
