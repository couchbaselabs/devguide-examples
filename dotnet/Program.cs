using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DevGuide
{
    public class Program
    {
        static void Main(string[] args)
        {
            Task.Run(() => AllExamples());

            Console.ReadLine();
        }

        private static async void AllExamples()
        {
            await new ConnectionBase().ExecuteAsync();
            await new ConnectionConfig().ExecuteAsync();
            await new Retrieve().ExecuteAsync();
            await new Update().ExecuteAsync();
            await new BulkInsert().ExecuteAsync();
            await new BulkGet().ExecuteAsync();
            await new FieldEncryptionAes().ExecuteAsync();
        }
    }
}
