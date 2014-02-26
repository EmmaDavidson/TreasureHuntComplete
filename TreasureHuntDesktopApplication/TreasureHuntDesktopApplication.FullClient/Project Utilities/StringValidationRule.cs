using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Controls;

namespace TreasureHuntDesktopApplication.FullClient.Project_Utilities
{
    //-http://msdn.microsoft.com/en-us/library/system.windows.controls.validationrule%28v=vs.110%29.aspx
    //-http://msdn.microsoft.com/en-us/library/system.windows.data.binding.validationrules%28v=vs.110%29.aspx
    class StringValidationRule : ValidationRule
    {
        public override ValidationResult Validate(object value, CultureInfo cultureInfo)
        {
                //Use this rule to ensure that the box is not empty. 
                //Would need to be refactored to allow other types of validation for each particular box!
                String stringItem = value as string; //Taking in the value that is being assessed and treating it like a string

                if(String.IsNullOrEmpty(stringItem))
                {
                    return new ValidationResult(false, "Please fill the field");
                }

                return new ValidationResult(true, null); 
        }
    }
}
