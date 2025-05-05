import React, { useState } from 'react';
import { 
  Cog6ToothIcon,
  KeyIcon,
  BellIcon,
  ShieldCheckIcon,
} from '@heroicons/react/24/outline';
import Container from '@mui/material/Container';
import Typography from '@mui/material/Typography';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Grid from '@mui/material/Grid';
import Box from '@mui/material/Box';
import Switch from '@mui/material/Switch';
import TextField from '@mui/material/TextField';

const settings = [
  {
    id: 'integrations',
    name: 'Repository Integrations',
    description: 'Connect your GitHub, GitLab, or Bitbucket repositories',
    icon: KeyIcon,
    fields: [
      { id: 'github', name: 'GitHub', type: 'toggle', value: true },
      { id: 'gitlab', name: 'GitLab', type: 'toggle', value: false },
      { id: 'bitbucket', name: 'Bitbucket', type: 'toggle', value: false },
    ],
  },
  {
    id: 'notifications',
    name: 'Notifications',
    description: 'Configure how you want to be notified about code reviews',
    icon: BellIcon,
    fields: [
      { id: 'email', name: 'Email Notifications', type: 'toggle', value: true },
      { id: 'slack', name: 'Slack Notifications', type: 'toggle', value: false },
      { id: 'webhook', name: 'Webhook Notifications', type: 'toggle', value: false },
    ],
  },
  {
    id: 'security',
    name: 'Security Settings',
    description: 'Manage your security preferences and API keys',
    icon: ShieldCheckIcon,
    fields: [
      { id: 'apiKey', name: 'API Key', type: 'text', value: '••••••••••••••••' },
      { id: '2fa', name: 'Two-Factor Authentication', type: 'toggle', value: false },
    ],
  },
];

const Settings: React.FC = () => {
  const [settingsState, setSettingsState] = useState(settings);

  const handleToggle = (sectionId: string, fieldId: string) => {
    setSettingsState(prevSettings =>
      prevSettings.map(section => {
        if (section.id === sectionId) {
          return {
            ...section,
            fields: section.fields.map(field => {
              if (field.id === fieldId) {
                return { ...field, value: !field.value };
              }
              return field;
            }),
          };
        }
        return section;
      })
    );
  };

  return (
    <Box py={6}>
      <Container maxWidth="md">
        <Box display="flex" alignItems="center" mb={4}>
          <Cog6ToothIcon style={{ height: 32, width: 32, color: '#888' }} />
          <Typography variant="h4" fontWeight={700} ml={2}>
            Settings
          </Typography>
        </Box>
        <Grid container spacing={4}>
          {settingsState.map((section) => (
            <Grid item xs={12} key={section.id}>
              <Card elevation={2}>
                <CardContent>
                  <Box display="flex" alignItems="center" mb={1}>
                    <section.icon style={{ height: 24, width: 24, color: '#6366f1' }} />
                    <Typography variant="h6" fontWeight={700} ml={1}>
                      {section.name}
                    </Typography>
                  </Box>
                  <Typography variant="body2" color="text.secondary" mb={2}>
                    {section.description}
                  </Typography>
                  <Grid container spacing={2}>
                    {section.fields.map((field) => (
                      <Grid item xs={12} sm={6} key={field.id}>
                        <Box display="flex" alignItems="center" justifyContent="space-between">
                          <Typography variant="body1">{field.name}</Typography>
                          {field.type === 'toggle' ? (
                            <Switch
                              checked={Boolean(field.value)}
                              onChange={() => handleToggle(section.id, field.id)}
                              color="primary"
                            />
                          ) : (
                            <TextField
                              value={typeof field.value === 'string' ? field.value : ''}
                              InputProps={{ readOnly: true }}
                              variant="outlined"
                              size="small"
                              sx={{ minWidth: 180 }}
                            />
                          )}
                        </Box>
                      </Grid>
                    ))}
                  </Grid>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      </Container>
    </Box>
  );
};

export default Settings; 