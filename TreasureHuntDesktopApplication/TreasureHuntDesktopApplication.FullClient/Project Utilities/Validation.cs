using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;
using System.Windows.Controls;

namespace TreasureHuntDesktopApplication.FullClient.Project_Utilities
{
    public static class Validation
    {
        public static bool IsNullOrWhiteSpace(String stringToCheck)
        {
                if (String.IsNullOrWhiteSpace(stringToCheck))
                {
                    return true;
                }
            
            return false;
        }

        public static bool IsNullOrEmpty(String stringToCheck)
        {

            if (String.IsNullOrEmpty(stringToCheck) || String.IsNullOrWhiteSpace(stringToCheck))
            {
                return true;
            }
            return false;
        }

        public static bool IsValidLength(String stringToCheck, int maxLength, int minLength)
        {
            if(stringToCheck != null)
            {
                if (stringToCheck.Length <= maxLength && stringToCheck.Length >= minLength)
                {
                    return true;
                }
            }
            return false;
        }

        public static bool IsValidCharacters(String stringToCheck)
        {
            if (stringToCheck != null)
            {
                if (Regex.IsMatch(stringToCheck, @"^[a-zA-Z ]+$"))
                {
                    return true;
                }
            }
            return false;
        }

        
        public static bool IsValidEmail(String emailToCheck)
        {
            if (emailToCheck != null)
            {
                //-http://stackoverflow.com/questions/16167983/best-regular-expression-for-email-validation-in-c-sharp
                if (Regex.IsMatch(emailToCheck, @"^([\w\.\-]+)@([\w\-]+)((\.(\w){2,3})+)$"))
                {
                    return true;
                }
            }
            return false;
        }

        public static bool ArePasswordsMatching(String password, String retypedPassword)
        {
            if (password != null && retypedPassword != null)
            {
                if (password.SequenceEqual(retypedPassword))
                {
                    return true;
                }
            }
            return false;
        }

    }
}
