﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using TreasureHuntDesktopApplication.FullClient.TreasureHuntService;

//----------------------------------------------------------
//<copyright>
//</copyright>
//----------------------------------------------------------

namespace TreasureHuntDesktopApplication.FullClient.Messages
{
    /// <Summary> The purpose of this class is to be a framework for sending messages and data about the current user. </Summary>

    public class CurrentUserMessage
    {
        public user CurrentUser { get; set; }
    }
}
