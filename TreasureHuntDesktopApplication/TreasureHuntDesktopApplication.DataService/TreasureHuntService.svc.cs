using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Serialization;
using System.ServiceModel;
using System.Text;
using TreasureHuntDesktopApplication.Data;

namespace TreasureHuntDesktopApplication.DataService
{
   
    public class TreasureHuntService : ITreasureHuntService
    {
        public IEnumerable<hunt> GetTreasureHunts()
        {
            using (var context = new TreasureHuntEntities())
            {
                var returnedHunts = context.hunts.ToList();
                returnedHunts.ForEach(e => context.ObjectStateManager.ChangeObjectState(e,System.Data.EntityState.Detached));        
                return returnedHunts;
              
            }
        }

        public IEnumerable<hunt> GetTreasureHuntsForParticularUser(user user) 
        {
            using (var context = new TreasureHuntEntities())
            {
                if (user == null) return null;
                var returnedHuntIds = context.userhunts.Where(c => c.UserId == user.UserId).Select(s => s.HuntId).ToList();
                returnedHuntIds.ForEach(e => context.ObjectStateManager.ChangeObjectState(e, System.Data.EntityState.Detached));

                List<hunt> returnedListOfHunts = new List<hunt>();

                foreach (var huntId in returnedHuntIds)
                {
                    var hunt = context.hunts.Where(c => c.HuntId == huntId).Single();
                    context.ObjectStateManager.ChangeObjectState(hunt, System.Data.EntityState.Detached);
                    returnedListOfHunts.Add(hunt);
                }

                return returnedListOfHunts;
            }
        }

        public IEnumerable<question> GetHuntQuestions(hunt hunt)
        { 
            using (var context = new TreasureHuntEntities())
            {
                if (hunt == null) return null;
                var returnedquestionIds = context.huntquestions.Where(c => c.HuntId == hunt.HuntId).Select(s => s.QuestionId).ToList();
                returnedquestionIds.ForEach(e => context.ObjectStateManager.ChangeObjectState(e, System.Data.EntityState.Detached));

                List<question> returnedListOfQuestions = new List<question>();

                foreach (var questionId in returnedquestionIds)
                {
                    var question = context.questions.Where(c => c.QuestionId == questionId).Single();
                    context.ObjectStateManager.ChangeObjectState(question, System.Data.EntityState.Detached);
                    returnedListOfQuestions.Add(question);
                }

                return returnedListOfQuestions;
            }
        }

        public question GetQuestion(long questionId)
        {
            using (var context = new TreasureHuntEntities())
            {
                var returnedQuestions = context.questions.Where(c => c.QuestionId == questionId).Single();
                context.ObjectStateManager.ChangeObjectState(returnedQuestions, System.Data.EntityState.Detached);
                return returnedQuestions;
            }
        }

        public long SaveQuestion(question newQuestion)
        {
            using (var context = new TreasureHuntEntities())
            {
                context.questions.AddObject(newQuestion);
                context.SaveChanges();
                context.ObjectStateManager.ChangeObjectState(newQuestion, System.Data.EntityState.Added);
                return newQuestion.QuestionId;
            }
        }
     
        public void SaveNewHuntQuestion(huntquestion huntQuestion)
        {
            using (var context = new TreasureHuntEntities())
            {
                context.huntquestions.AddObject(huntQuestion);
                context.SaveChanges();
                context.ObjectStateManager.ChangeObjectState(huntQuestion, System.Data.EntityState.Added);
            }
        }

        public long SaveNewHunt(hunt newHunt)
        {
            using (var context = new TreasureHuntEntities())
            {
                context.hunts.AddObject(newHunt);
                context.SaveChanges();
                context.ObjectStateManager.ChangeObjectState(newHunt, System.Data.EntityState.Added);
                return newHunt.HuntId;
            }
        }

        public void UpdateQuestion(question updatedQuestion)
        {
            using (var context = new TreasureHuntEntities())
            {
                context.questions.AddObject(updatedQuestion);
                context.SaveChanges();
                context.ObjectStateManager.ChangeObjectState(updatedQuestion, System.Data.EntityState.Modified);
            }
        }

        public hunt GetHuntBasedOnName(String name, long userId)
        {
            using (var context = new TreasureHuntEntities())
            {
                //Get all of this users hunts ids
                var returnedUserHunts = context.userhunts.Where(c => c.UserId == userId).ToList();
                context.ObjectStateManager.ChangeObjectState(returnedUserHunts, System.Data.EntityState.Detached);

                //For each of the hunt ids 
                foreach (var userHunts in returnedUserHunts)
                {
                    //get that hunt
                    var hunt = context.hunts.Where(c => c.HuntId == userHunts.HuntId).Single();
                    context.ObjectStateManager.ChangeObjectState(hunt, System.Data.EntityState.Detached);

                    //if that hunt name is the same as expected, return that hunt
                    if (hunt.HuntName == name)
                    {
                        return hunt;
                    }
                }

                return null;
            }
        }

        public List<user> GetExistingUsers()
        {
            using (var context = new TreasureHuntEntities())
            {
                var returnedUsers = context.users.ToList();
                returnedUsers.ForEach(e => context.ObjectStateManager.ChangeObjectState(e, System.Data.EntityState.Detached));
                return returnedUsers;
            }
        }

        public long SaveUser(user newUser)
        {
            using (var context = new TreasureHuntEntities())
            {
                context.users.AddObject(newUser);
                context.SaveChanges();
                context.ObjectStateManager.ChangeObjectState(newUser, System.Data.EntityState.Added);
                return newUser.UserId;
            }
        }

        public void SaveUserRole(userrole newUserRole)
        {
            using (var context = new TreasureHuntEntities())
            {
                context.userroles.AddObject(newUserRole);
                context.SaveChanges();
                context.ObjectStateManager.ChangeObjectState(newUserRole, System.Data.EntityState.Added);
            }
        }

        public user GetUser(string emailAddress)
        {
            using (var context = new TreasureHuntEntities())
            {
                if (context.users.Any(c => c.Email == emailAddress))
                {
                    var returnedUser = context.users.Where(c => c.Email == emailAddress).Single();
                    context.ObjectStateManager.ChangeObjectState(returnedUser, System.Data.EntityState.Detached);
                    return returnedUser;
                }

                return null;
            }
        }

        public userrole GetUserRole(user user)
        {
            using (var context = new TreasureHuntEntities())
            {
                var returnedUserRole = context.userroles.Where(c => c.UserId == user.UserId).Single();
                context.ObjectStateManager.ChangeObjectState(returnedUserRole, System.Data.EntityState.Detached);
                return returnedUserRole;
            }
        }

        public void SaveUserHunt(userhunt userHunt)
        {
            using (var context = new TreasureHuntEntities())
            {
                context.userhunts.AddObject(userHunt);
                context.SaveChanges();
                context.ObjectStateManager.ChangeObjectState(userHunt, System.Data.EntityState.Added);
            }
        }
        //-http://stackoverflow.com/questions/15576282/sorting-a-c-sharp-list-by-multiple-columns
        //-http://stackoverflow.com/questions/18525253/wpf-listt-sort
        public List<huntparticipant> GetHuntParticipants(hunt currentTreasureHunt)
        {
            using (var context = new TreasureHuntEntities())
            {
                if (currentTreasureHunt == null) return null;
                var returnedHuntParticipants = context.huntparticipants.Where(c => c.HuntId == currentTreasureHunt.HuntId).OrderByDescending(c => c.ElapsedTime).ThenByDescending(s => s.Tally).ToList();
                returnedHuntParticipants.ForEach(e => context.ObjectStateManager.ChangeObjectState(e, System.Data.EntityState.Detached));
                return returnedHuntParticipants;
            }
        }

        public user GetParticipantName(long userId)
        {
            using (var context = new TreasureHuntEntities())
            {
                var returnedHuntParticipant = context.users.Where(c => c.UserId == userId).Single();
                context.ObjectStateManager.ChangeObjectState(returnedHuntParticipant, System.Data.EntityState.Detached);
                return returnedHuntParticipant;
            }
        }

        public void SaveUserSecurityQuestion(usersecurityquestion userSecurityQuestion)
        {
            using (var context = new TreasureHuntEntities())
            {
                context.usersecurityquestions.AddObject(userSecurityQuestion);
                context.SaveChanges();
                context.ObjectStateManager.ChangeObjectState(userSecurityQuestion, System.Data.EntityState.Added);
            }
        }

        public IEnumerable<securityquestion> getListOfSecurityQuestions()
        {
            using (var context = new TreasureHuntEntities())
            {
                var returnedQuestions = context.securityquestions.ToList();
                returnedQuestions.ForEach(e => context.ObjectStateManager.ChangeObjectState(e, System.Data.EntityState.Detached));
                return returnedQuestions;
            }
        }

        public securityquestion getUserSecurityQuestion(user user)
        {
            using (var context = new TreasureHuntEntities())
            {
                if (user == null) return null;

                var returnedUserSecurityQuestionId = context.usersecurityquestions.Where(c => c.UserId == user.UserId).Select(s => s.SecurityQuestionId).Single();
                context.ObjectStateManager.ChangeObjectState(returnedUserSecurityQuestionId, System.Data.EntityState.Detached);

                var returnedQuestion = context.securityquestions.Where(c => c.SecurityQuestionId == returnedUserSecurityQuestionId).Single();
                context.ObjectStateManager.ChangeObjectState(returnedQuestion, System.Data.EntityState.Detached);

                return returnedQuestion;
            }
        }

        public usersecurityquestion getUserSecurityAnswer(user currentUser)
        {
            using (var context = new TreasureHuntEntities())
                {
                    if (currentUser == null) return null;

                    var returnedUserSecurityQuestionDetails = context.usersecurityquestions.Where(c => c.UserId == currentUser.UserId).Single();
                    context.ObjectStateManager.ChangeObjectState(returnedUserSecurityQuestionDetails, System.Data.EntityState.Detached);

                    return returnedUserSecurityQuestionDetails;
                }
        }

        public void updateUserPassword(user currentUser, String newPassword)
        { 
            using (var context = new TreasureHuntEntities())
            {
                var userToChange = context.users.Where(c => c.UserId == currentUser.UserId).Single();
                userToChange.Password = newPassword;
                context.SaveChanges();
                context.ObjectStateManager.ChangeObjectState(userToChange, System.Data.EntityState.Modified);
            }
        }

        public void updateCompanyPassword(user currentUser, String newPassword)
        {
            using (var context = new TreasureHuntEntities())
            {
                var companyToChange = context.companydetails.Where(c => c.UserId == currentUser.UserId).Single();
                companyToChange.CompanyPassword = newPassword;
                context.SaveChanges();
                context.ObjectStateManager.ChangeObjectState(companyToChange, System.Data.EntityState.Modified);
            }
        }

        
         public void saveCompany(companydetail companyDetails)
          {
               using(var context = new TreasureHuntEntities())
                {
                    context.companydetails.AddObject(companyDetails);
                     context.SaveChanges();
                     context.ObjectStateManager.ChangeObjectState(companyDetails, System.Data.EntityState.Added);
                }
          }

         public List<companydetail> getExistingCompanies()
         {
             using (var context = new TreasureHuntEntities())
             {
                 var returnedCompanies = context.companydetails.ToList();
                 returnedCompanies.ForEach(e => context.ObjectStateManager.ChangeObjectState(e, System.Data.EntityState.Detached));
                 return returnedCompanies;

             }
         }
         
    }
}
