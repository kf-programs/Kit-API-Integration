import { useState, useEffect } from 'react';
import axios from 'axios';

{ /* This component allows users to tag subscribers with a specific tag using a CSV file upload 
    * It fetches tags from the Kit API and allows users to select a tag and upload a CSV file - one column of email addresses.
    * After processing, it displays a summary of the tagging operation and details for each email.
    * 
    * Importantly, unlike with the Kit web app, it will not add the email to the mailing list if the email is not found
    */ }
function TagSubscribers() {
  const [apiKey, setApiKey] = useState('');
  const [file, setFile] = useState(null);
  
  const [isLoading, setIsLoading] = useState(false);

  const [tags, setTags] = useState([]);
  const [selectedTag, setSelectedTag] = useState('');
  
  const [error, setError] = useState('');
  const [showDetails, setShowDetails] = useState(false);
  const [summaryData, setSummaryData] = useState(null);
  const [emailDetailsData, setEmailDetailsData] = useState([]);

  { /* Fetch tags when the component mounts or apiKey changes 
    * Currently, the apiKey won't be set until the user inputs it, so this will only run after the first input.
    */ }
  useEffect(() => {
    if (apiKey) {
      fetchTags();
    }
  }, [apiKey]);

  const fetchTags = async () => {
    try {
      const response = await axios.get('/api/tags', {
        headers: { 'Kit-Api-Key': apiKey }
      });
      setTags(response.data);
    } catch (err) {
      setError('Failed to fetch tags: ' + err.message);
    }
  };

  const handleFileUpload = (event) => {
    const file = event.target.files[0];
    if (file && file.type !== 'text/csv') {
      setError('Please upload a CSV file');
      return;
    }
    setFile(file);
    setError('');
  };

  const handleSubmit = async () => {
    if (!file) {
      setError('Please select a file');
      return;
    }
    if (!selectedTag) {
      setError('Please select a tag');
      return;
    }

    setIsLoading(true);
    setError('');
    setSummaryData(null);
    setEmailDetailsData([]);

    try {
      const reader = new FileReader();
      reader.onload = async (e) => {
        const text = e.target.result;
        const emails = text.split('\n')
          .map(line => line.trim())
          .filter(line => line && line.includes('@'));

        const response = await axios.post('/api/tag-subscribers', 
          { emails, tagId: selectedTag },
          {
            headers: {
              'Kit-Api-Key': apiKey,
              'Content-Type': 'application/json',
            },
          }
        );

        const { message, details, emailDetails } = response.data;
        setSummaryData({ message, details });
        setEmailDetailsData(emailDetails || []);
      };
      reader.readAsText(file);
    } catch (err) {
      setError('Failed to process CSV: ' + err.message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto', padding: '20px' }}>
      <div style={{ 
        display: 'flex', 
        flexDirection: 'column',
        alignItems: 'center',
        width: '100%'
      }}>
        {/* Input field container */}
        <div style={{ width: '100%', textAlign: 'center', marginBottom: '20px' }}>
          <input
            type="text"
            placeholder="Enter Kit API Key"
            value={apiKey}
            onChange={(e) => setApiKey(e.target.value)}
            style={{ padding: '8px', width: '300px' }}
          />
        </div>

        {/* Tags container */}
        {tags.length > 0 && (
          <div style={{ width: '100%', textAlign: 'center', marginBottom: '20px' }}>
            <h3>Select Tag</h3>
            <div style={{ 
              display: 'flex', 
              flexDirection: 'column', 
              alignItems: 'left',
              gap: '10px' 
            }}>
              {tags.map(tag => (
                <label key={tag.id} style={{ display: 'flex', alignItems: 'left' }}>
                  <input
                    type="radio"
                    name="tag"
                    value={tag.id}
                    checked={selectedTag === tag.id}
                    onChange={(e) => setSelectedTag(e.target.value)}
                    style={{ marginRight: '8px' }}
                  />
                  {tag.name}
                </label>
              ))}
            </div>
          </div>
        )}

        {/* File upload container */}
        <div style={{ width: '100%', textAlign: 'center', marginBottom: '20px' }}>
          <input
            type="file"
            accept=".csv"
            onChange={handleFileUpload}
            style={{ marginBottom: '10px' }}
          />
        </div>
        <div style={{ width: '100%', textAlign: 'center', marginBottom: '20px' }}>
          <button 
            onClick={handleSubmit}
            disabled={isLoading || !file}
            style={{ padding: '8px 16px' }}
          >
            {isLoading ? 'Processing...' : 'Upload and Tag'}
          </button>
        </div>

        {/* Error and results container */}
        <div style={{ width: '100%', textAlign: 'center' }}>
          {error && <p style={{ color: 'red' }}>{error}</p>}
          
          {summaryData && (
            <div>
              <p>{summaryData.message}</p>
              <div style={{ marginTop: '10px', fontSize: '0.9em' }}>
                <p style={{ color: '#28a745' }}>✓ Successfully tagged: {summaryData.details.success}</p>
                <p style={{ color: '#17a2b8' }}>ℹ Already tagged: {summaryData.details.alreadyTagged}</p>
                {summaryData.details.failed > 0 && (
                  <p style={{ color: '#dc3545' }}>✗ Failed to tag: {summaryData.details.failed}</p>
                )}
                {emailDetailsData.length > 0 && (
                  <div style={{ width: '100%' }}>
                    <button
                      onClick={() => setShowDetails(!showDetails)}
                      style={{
                        marginTop: '10px',
                        padding: '5px 10px',
                        backgroundColor: '#f0f0f0',
                        border: '1px solid #ddd',
                        borderRadius: '4px',
                        cursor: 'pointer'
                      }}
                    >
                      {showDetails ? 'Hide Details ▼' : 'Show Details ▶'}
                    </button>
                    {showDetails && (
                      <div style={{ 
                        width: '100%',
                        marginTop: '20px', 
                        padding: '10px', 
                        backgroundColor: '#f8f9fa', 
                        borderRadius: '4px',
                        textAlign: 'left',
                        boxSizing: 'border-box'
                      }}>
                        {emailDetailsData.map((emailDetail, index) => (
                          <div key={index} style={{ marginBottom: '15px', borderBottom: '1px solid #eee', paddingBottom: '10px' }}>
                            <p style={{ fontWeight: 'bold', marginBottom: '5px' }}>{emailDetail.email}</p>
                            <ul style={{ listStyle: 'none', margin: 0, padding: 0 }}>
                              <li style={{ margin: '5px 0' }}>
                                Status: {emailDetail.status}
                              </li>
                              <li style={{ margin: '5px 0' }}>
                                Result: {emailDetail.result}
                              </li>
                            </ul>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default TagSubscribers;
