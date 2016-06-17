using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Couchbase;

namespace DevGuide
{
    class AsyncBatch : ConnectionBase
    {
        static void Main(string[] args)
        {
            new AsyncBatch().ExecuteAsync().Wait();
            Console.Read();
        }

        public override async Task ExecuteAsync()
        {
            var ids = new List<string> { "doc1", "doc2", "doc4" };
            await PrintAllDocumentsAsync(ids);
        }

        public async Task PrintAllDocumentsAsync(List<string> ids)
        {
            var tasks = new List<Task<IDocumentResult<string>>>();
            ids.ForEach(x => tasks.Add(_bucket.GetDocumentAsync<string>(x)));

            var results = await Task.WhenAll(tasks);
            results.ToList().ForEach(doc => Console.WriteLine(doc.Status));
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
