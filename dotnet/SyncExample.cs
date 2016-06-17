using System;
using System.Threading;
using System.Threading.Tasks;

namespace DevGuide
{
    public class SyncExample : ConnectionBase
    {
        static void Main(string[] args)
        {
            Console.WriteLine("Before calling PrintDocumentAsync on thread {0}.",
              Thread.CurrentThread.ManagedThreadId);

            new SyncExample().ExecuteAsync().Wait();


            Console.WriteLine("After calling PrintDocumentAsync on thread {0}.",
                Thread.CurrentThread.ManagedThreadId);
        }

        public override Task ExecuteAsync()
        {
            //call it synchronously with no await
            PrintDocumentAsync("somekey").Wait();

            return Task.FromResult(0);
        }

        public Task PrintDocumentAsync(string id)
        {
            Console.WriteLine("Before awaiting GetDocumentAsync on thread {0}.",
                Thread.CurrentThread.ManagedThreadId);

            var doc = _bucket.GetDocumentAsync<string>(id).Result;

            Console.WriteLine("After awaiting GetDocumentAsync on thread {0}.",
                Thread.CurrentThread.ManagedThreadId);

            Console.WriteLine(doc.Content);

            return Task.FromResult(0);
        }
    }
}

#region [ License information          ]

/* ************************************************************
 *
 *    @author Couchbase <info@couchbase.com>
 *    @copyright 2015 Couchbase, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 * ************************************************************/

#endregion
