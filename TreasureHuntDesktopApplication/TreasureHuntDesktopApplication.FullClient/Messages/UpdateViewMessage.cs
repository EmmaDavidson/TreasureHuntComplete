﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

//----------------------------------------------------------
//<copyright>
//Emma Davidson - Treasure Hunt 2013-3014 Final Year Project
//</copyright>
//----------------------------------------------------------

namespace TreasureHuntDesktopApplication.FullClient.Messages
{
    /// <Summary> The purpose of this class is to be a framework for sending messages and data about what view to 
    /// navigate the user to on screen. </Summary>

    public class UpdateViewMessage
    {
        public String UpdateViewTo { get; set; }
    }
}
